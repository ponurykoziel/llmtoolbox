"""
gentools.py — Generate tool YAML files from an OpenAPI 3.x spec.

Usage:
    python gentools.py openapi-spec.json tools/
    python gentools.py openapi-spec.yaml tools/

Also usable as a library:
    from gentools import generate_from_url, generate_from_dict

The spec must be local (no remote $refs). One .yaml file per operation.
Existing files in the output dir are overwritten.
"""

import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional

import httpx
import yaml


def _resolve_ref(spec: dict, ref: str) -> dict:
    """Resolve a local $ref like '#/components/schemas/Foo'."""
    if not ref.startswith("#/"):
        raise ValueError(f"Only local $ref supported, got: {ref}")
    parts = ref[2:].split("/")
    current: Any = spec
    for p in parts:
        current = current[p]
    return current


def _schema_to_params(schema: dict, spec: dict) -> dict:
    """Convert a JSON Schema (possibly $ref) to tool parameters format."""
    if "$ref" in schema:
        schema = _resolve_ref(spec, schema["$ref"])

    result: dict = {
        "type": schema.get("type", "object"),
        "properties": {},
    }

    for prop_name, prop_schema in schema.get("properties", {}).items():
        entry: dict = {"type": prop_schema.get("type", "string")}
        if "description" in prop_schema:
            entry["description"] = prop_schema["description"]
        result["properties"][prop_name] = entry

    if schema.get("required"):
        result["required"] = schema["required"]

    return result


def _build_body_template(schema: dict, spec: dict) -> str:
    """Build a JSON body template with {param} placeholders."""
    if "$ref" in schema:
        schema = _resolve_ref(spec, schema["$ref"])

    props = schema.get("properties", {})
    parts = [f'"{name}":"{{{name}}}"' for name in props]
    return "{" + ",".join(parts) + "}"


def _collect_params(operation: dict, spec: dict) -> dict:
    """Merge requestBody schema and explicit parameters into one params dict."""
    params: dict = {"type": "object", "properties": {}}
    required: List[str] = []

    # requestBody
    rb = operation.get("requestBody", {})
    json_media = rb.get("content", {}).get("application/json", {})
    if json_media:
        schema = json_media.get("schema", {})
        ps = _schema_to_params(schema, spec)
        params["properties"].update(ps.get("properties", {}))
        if ps.get("required"):
            required.extend(ps["required"])

    # explicit parameters (path, query, header)
    for param in operation.get("parameters", []):
        pname = param["name"]
        pschema = param.get("schema", {"type": "string"})
        entry: dict = {"type": pschema.get("type", "string")}
        if "description" in param:
            entry["description"] = param["description"]
        params["properties"][pname] = entry
        if param.get("required"):
            required.append(pname)

    if required:
        params["required"] = required

    return params


def _build_url(path_template: str, operation: dict) -> str:
    """Return the URL template. Path params stay as {param}, query params appended."""
    url = path_template

    query_parts = []
    for param in operation.get("parameters", []):
        if param["in"] == "query":
            pname = param["name"]
            query_parts.append(f"{pname}={{{pname}}}")

    if query_parts:
        url += "?" + "&".join(query_parts)

    return url


def _build_headers(operation: dict) -> dict:
    """Collect static header values from parameters marked 'in: header'."""
    headers: dict = {}
    for param in operation.get("parameters", []):
        if param["in"] == "header":
            pschema = param.get("schema", {})
            default = pschema.get("default")
            if default is not None:
                headers[param["name"]] = str(default)
            else:
                headers[param["name"]] = f"{{{param['name']}}}"
    return headers


def generate_from_dict(spec: dict, output_dir: str) -> int:
    """Generate tool YAML files from an already-parsed OpenAPI spec dict.

    Returns count of tools written.
    """
    if "paths" not in spec:
        return 0

    out = Path(output_dir)
    out.mkdir(parents=True, exist_ok=True)

    count = 0
    for path_url, path_item in spec["paths"].items():
        for method, operation in path_item.items():
            if method not in ("get", "post", "put", "patch", "delete"):
                continue

            op_id = operation.get("operationId")
            if not op_id:
                op_id = path_url.strip("/").replace("/", "_").replace("-", "_") + "_" + method

            summary = operation.get("summary", "") or operation.get("description", "") or ""

            params = _collect_params(operation, spec)
            url = _build_url(path_url, operation)
            headers = _build_headers(operation)
            body_template = ""

            rb = operation.get("requestBody", {})
            json_media = rb.get("content", {}).get("application/json", {})
            if json_media:
                schema = json_media.get("schema", {})
                body_template = _build_body_template(schema, spec)

            tool: dict = {
                "name": op_id,
                "description": summary,
                "parameters": params,
                "endpoint": {
                    "url": url,
                    "method": method.upper(),
                },
            }

            if headers:
                tool["endpoint"]["headers"] = headers
            if body_template:
                tool["endpoint"]["body_template"] = body_template

            out_path = out / f"{op_id}.yaml"
            out_path.write_text(yaml.dump(
                tool,
                default_flow_style=False,
                allow_unicode=True,
                sort_keys=False,
                indent=2,
            ))
            count += 1

    return count


async def generate_from_url(url: str, output_dir: str, timeout: float = 30) -> int:
    """Fetch an OpenAPI spec from a URL and generate tool YAML files.

    Returns count of tools written, or 0 on failure.
    """
    try:
        async with httpx.AsyncClient(timeout=timeout) as client:
            r = await client.get(url)
            r.raise_for_status()

        content_type = r.headers.get("content-type", "")
        text = r.text

        if "json" in content_type or url.endswith(".json"):
            spec = json.loads(text)
        else:
            spec = yaml.safe_load(text)

        return generate_from_dict(spec, output_dir)

    except Exception:
        import logging
        logging.getLogger("gentools").exception("failed to fetch/generate from %s", url)
        return 0


def generate(spec_path: str, output_dir: str) -> int:
    """Read a local spec file and generate tool YAML files."""
    raw = Path(spec_path).read_text()
    spec = json.loads(raw) if spec_path.endswith(".json") else yaml.safe_load(raw)
    return generate_from_dict(spec, output_dir)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("usage: python gentools.py <openapi-spec.json|yaml> <output-dir>", file=sys.stderr)
        sys.exit(1)

    n = generate(sys.argv[1], sys.argv[2])
    print(f"\n{n} tool(s) written to {sys.argv[2]}")
