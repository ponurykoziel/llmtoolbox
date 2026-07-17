package com.sheahorn.llmtoolbox.buildtools.git;

import com.sheahorn.llmtoolbox.execution.ExecutionResponse;
import com.sheahorn.llmtoolbox.execution.Executor;
import com.sheahorn.llmtoolbox.execution.ToolSupport;
import com.sheahorn.llmtoolbox.fstools.info.FsResourceSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/api/tools/devops/git")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GitResource extends FsResourceSupport {

    @Inject
    Executor executor;

    @Inject
    GitLockService lock;

    // ── read-only ────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_status",
            summary = "Runs `git status` (human-readable) inside the repo path"
    )
    @POST
    @Path("/status")
    public ExecutionResponse status(GitRequestDto request) throws Exception {
        return runInRepo(request, "git status");
    }

    @Operation(
            operationId = "devops_git_log_recent",
            summary = "Runs `git log --oneline -n <n>` inside the repo path (defaults to 20)"
    )
    @POST
    @Path("/log-recent")
    public ExecutionResponse logRecent(GitRequestDto request) throws Exception {
        int n = (request != null && request.n != null && request.n > 0) ? request.n : 20;
        return runInRepo(request, "git log --oneline -n " + n);
    }

    @Operation(
            operationId = "devops_git_log_oneline",
            summary = "Runs `git log --oneline` (full history, no limit) inside the repo path"
    )
    @POST
    @Path("/log-oneline")
    public ExecutionResponse logOneline(GitRequestDto request) throws Exception {
        return runInRepo(request, "git log --oneline");
    }

    @Operation(
            operationId = "devops_git_log",
            summary = "Runs `git log` (full log, default format) inside the repo path"
    )
    @POST
    @Path("/log")
    public ExecutionResponse log(GitRequestDto request) throws Exception {
        return runInRepo(request, "git log");
    }

    @Operation(
            operationId = "devops_git_diff",
            summary = "Runs `git diff` inside the repo path"
    )
    @POST
    @Path("/diff")
    public ExecutionResponse diff(GitRequestDto request) throws Exception {
        return runInRepo(request, "git diff");
    }

    @Operation(
            operationId = "devops_git_diff_staged",
            summary = "Runs `git diff --staged` inside the repo path"
    )
    @POST
    @Path("/diff-staged")
    public ExecutionResponse diffStaged(GitRequestDto request) throws Exception {
        return runInRepo(request, "git diff --staged");
    }

    // ── info ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_print_remote",
            summary = "Runs `git remote -v` to show remote names and URLs"
    )
    @POST
    @Path("/print-remote")
    public ExecutionResponse printRemote(GitRequestDto request) throws Exception {
        return runInRepo(request, "git remote -v");
    }

    @Operation(
            operationId = "devops_git_print_config",
            summary = "Runs `git config --list` to show local repo configuration"
    )
    @POST
    @Path("/print-config")
    public ExecutionResponse printConfig(GitRequestDto request) throws Exception {
        return runInRepo(request, "git config --list");
    }

    // ── staging / committing ─────────────────────────────────

    @Operation(
            operationId = "devops_git_add",
            summary = "Runs `git add <files>` inside the repo path. Use files=\".\" to stage all."
    )
    @POST
    @Path("/add")
    public ExecutionResponse add(GitRequestDto request) throws Exception {
        validatePath(request);
        String files = (request.files != null && !request.files.isBlank())
                ? request.files : ".";
        return runInRepo(request, "git add -- " + ToolSupport.shellQuote(files));
    }

    @Operation(
            operationId = "devops_git_commit",
            summary = "Runs `git commit -m <message>` inside the repo path"
    )
    @POST
    @Path("/commit")
    public ExecutionResponse commit(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.message == null || request.message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        return runInRepo(request, "git commit -m " + ToolSupport.shellQuote(request.message));
    }

    // ── reset / clean ────────────────────────────────────────

    @Operation(
            operationId = "devops_git_reset_mixed",
            summary = "Runs `git reset <commit>` (mixed reset). Defaults to HEAD if commit is not set."
    )
    @POST
    @Path("/reset-mixed")
    public ExecutionResponse resetMixed(GitRequestDto request) throws Exception {
        validatePath(request);
        String commit = (request.commit != null && !request.commit.isBlank())
                ? request.commit : "HEAD";
        return runInRepo(request, "git reset " + ToolSupport.shellQuote(commit));
    }

    @Operation(
            operationId = "devops_git_reset_soft",
            summary = "Runs `git reset --soft <commit>`. Defaults to HEAD if commit is not set."
    )
    @POST
    @Path("/reset-soft")
    public ExecutionResponse resetSoft(GitRequestDto request) throws Exception {
        validatePath(request);
        String commit = (request.commit != null && !request.commit.isBlank())
                ? request.commit : "HEAD";
        return runInRepo(request, "git reset --soft " + ToolSupport.shellQuote(commit));
    }

    @Operation(
            operationId = "devops_git_reset_hard",
            summary = "Runs `git reset --hard <commit>`. DESTRUCTIVE — discards working tree changes. Defaults to HEAD."
    )
    @POST
    @Path("/reset-hard")
    public ExecutionResponse resetHard(GitRequestDto request) throws Exception {
        validatePath(request);
        String commit = (request.commit != null && !request.commit.isBlank())
                ? request.commit : "HEAD";
        return runInRepo(request, "git reset --hard " + ToolSupport.shellQuote(commit));
    }

    @Operation(
            operationId = "devops_git_reset_hard_origin",
            summary = "Runs `git fetch origin <branch> && git reset --hard origin/<branch>`. DESTRUCTIVE. Requires confirm=true."
    )
    @POST
    @Path("/reset-hard-origin")
    public ExecutionResponse resetHardOrigin(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        if (!Boolean.TRUE.equals(request.confirm)) {
            throw new IllegalArgumentException("confirm=true is required for this destructive operation");
        }
        String path = normalizePath(request.path);
        String cmd = "cd -- " + ToolSupport.shellQuote(path)
                + " && git fetch origin " + ToolSupport.shellQuote(request.branch)
                + " && git reset --hard origin/" + ToolSupport.shellQuote(request.branch);
        return lock.runLocked(() -> executor.execute(cmd));
    }

    @Operation(
            operationId = "devops_git_clean",
            summary = "Runs `git clean -f -d` twice in sequence to remove untracked files and directories"
    )
    @POST
    @Path("/clean")
    public ExecutionResponse clean(GitRequestDto request) throws Exception {
        validatePath(request);
        String path = normalizePath(request.path);
        String cd = "cd -- " + ToolSupport.shellQuote(path) + " && ";
        // Directories sometimes survive the first run (e.g. nested git repos),
        // so we run clean twice to ensure everything is removed.
        String cmd = cd + "git clean -f -d && git clean -f -d";
        return lock.runLocked(() -> executor.execute(cmd));
    }

    // ── branch management ────────────────────────────────────

    @Operation(
            operationId = "devops_git_branch_list_local",
            summary = "Runs `git branch` to list local branches"
    )
    @POST
    @Path("/branch-list-local")
    public ExecutionResponse branchListLocal(GitRequestDto request) throws Exception {
        validatePath(request);
        return runInRepo(request, "git branch");
    }

    @Operation(
            operationId = "devops_git_branch_list_remote",
            summary = "Runs `git branch -r` to list remote-tracking branches"
    )
    @POST
    @Path("/branch-list-remote")
    public ExecutionResponse branchListRemote(GitRequestDto request) throws Exception {
        validatePath(request);
        return runInRepo(request, "git branch -r");
    }

    @Operation(
            operationId = "devops_git_branch_create",
            summary = "Runs `git branch <name>` to create a new branch"
    )
    @POST
    @Path("/branch-create")
    public ExecutionResponse branchCreate(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git branch " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_delete_if_merged",
            summary = "Runs `git branch -d <branch>` (safe delete — refuses if not merged)"
    )
    @POST
    @Path("/branch-delete-if-merged")
    public ExecutionResponse branchDeleteIfMerged(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git branch -d " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_delete_force",
            summary = "Runs `git branch -D <branch>` (force delete — even if not merged)"
    )
    @POST
    @Path("/branch-delete-force")
    public ExecutionResponse branchDeleteForce(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git branch -D " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_set_upstream",
            summary = "Runs `git branch --set-upstream-to=<remote>/<remoteBranch> <branch>`"
    )
    @POST
    @Path("/branch-set-upstream")
    public ExecutionResponse branchSetUpstream(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.remote == null || request.remote.isBlank()) {
            throw new IllegalArgumentException("remote is required");
        }
        if (request.remoteBranch == null || request.remoteBranch.isBlank()) {
            throw new IllegalArgumentException("remoteBranch is required");
        }
        String branch = (request.branch != null && !request.branch.isBlank())
                ? request.branch : "";
        String cmd = "git branch --set-upstream-to="
                + ToolSupport.shellQuote(request.remote + "/" + request.remoteBranch);
        if (!branch.isEmpty()) {
            cmd += " " + ToolSupport.shellQuote(branch);
        }
        return runInRepo(request, cmd);
    }

    // ── checkout ─────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_checkout_branch",
            summary = "Runs `git checkout <branch>` to switch to a branch (HEAD)"
    )
    @POST
    @Path("/checkout-branch")
    public ExecutionResponse checkoutBranch(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git checkout " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_checkout_commit",
            summary = "Runs `git checkout <commit>` to detach HEAD at a specific commit"
    )
    @POST
    @Path("/checkout-commit")
    public ExecutionResponse checkoutCommit(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.commit == null || request.commit.isBlank()) {
            throw new IllegalArgumentException("commit is required");
        }
        return runInRepo(request, "git checkout " + ToolSupport.shellQuote(request.commit));
    }

    @Operation(
            operationId = "devops_git_checkout_new_branch",
            summary = "Runs `git checkout -b <branch>` to create and switch to a new branch"
    )
    @POST
    @Path("/checkout-new-branch")
    public ExecutionResponse checkoutNewBranch(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git checkout -b " + ToolSupport.shellQuote(request.branch));
    }

    // ── merge ────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_merge",
            summary = "Runs `git merge <branch>` inside the repo path"
    )
    @POST
    @Path("/merge")
    public ExecutionResponse merge(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request, "git merge " + ToolSupport.shellQuote(request.branch));
    }

    // ── fetch ────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_fetch",
            summary = "Runs `git fetch <remote>` inside the repo path (defaults to origin)"
    )
    @POST
    @Path("/fetch")
    public ExecutionResponse fetch(GitRequestDto request) throws Exception {
        validatePath(request);
        String remote = (request.remote != null && !request.remote.isBlank())
                ? request.remote : "origin";
        return runInRepo(request, "git fetch " + ToolSupport.shellQuote(remote));
    }

    // ── push ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_push",
            summary = "Runs `git push <remote> <branch>` or `git push <remote> <branch>:<remoteBranch>` if remoteBranch is set"
    )
    @POST
    @Path("/push")
    public ExecutionResponse push(GitRequestDto request) throws Exception {
        validatePath(request);
        String remote = (request.remote != null && !request.remote.isBlank())
                ? request.remote : "origin";
        String branch = (request.branch != null && !request.branch.isBlank())
                ? request.branch : "";

        String cmd = "git push " + ToolSupport.shellQuote(remote);
        if (!branch.isEmpty()) {
            if (request.remoteBranch != null && !request.remoteBranch.isBlank()) {
                cmd += " " + ToolSupport.shellQuote(branch) + ":" + ToolSupport.shellQuote(request.remoteBranch);
            } else {
                cmd += " " + ToolSupport.shellQuote(branch);
            }
        }
        return runInRepo(request, cmd);
    }

    // ── pull ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_pull",
            summary = "Runs `git pull <remote> <branch>` inside the repo path (default merge)"
    )
    @POST
    @Path("/pull")
    public ExecutionResponse pull(GitRequestDto request) throws Exception {
        return doPull(request, "");
    }

    @Operation(
            operationId = "devops_git_pull_rebase",
            summary = "Runs `git pull --rebase <remote> <branch>` inside the repo path"
    )
    @POST
    @Path("/pull-rebase")
    public ExecutionResponse pullRebase(GitRequestDto request) throws Exception {
        return doPull(request, "--rebase");
    }

    @Operation(
            operationId = "devops_git_pull_ff",
            summary = "Runs `git pull --ff-only <remote> <branch>` inside the repo path"
    )
    @POST
    @Path("/pull-ff")
    public ExecutionResponse pullFf(GitRequestDto request) throws Exception {
        return doPull(request, "--ff-only");
    }

    private ExecutionResponse doPull(GitRequestDto request, String flag) throws Exception {
        validatePath(request);
        String remote = (request.remote != null && !request.remote.isBlank())
                ? request.remote : "origin";
        String branch = (request.branch != null && !request.branch.isBlank())
                ? request.branch : "";

        StringBuilder cmd = new StringBuilder("git pull");
        if (!flag.isEmpty()) {
            cmd.append(" ").append(flag);
        }
        cmd.append(" ").append(ToolSupport.shellQuote(remote));
        if (!branch.isEmpty()) {
            cmd.append(" ").append(ToolSupport.shellQuote(branch));
        }
        return runInRepo(request, cmd.toString());
    }

    // ── clone ────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_clone",
            summary = "Runs `git clone <url>` — path is the parent directory to clone into"
    )
    @POST
    @Path("/clone")
    public ExecutionResponse clone(GitRequestDto request) throws Exception {
        validatePath(request);
        if (request.url == null || request.url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }
        return runInRepo(request, "git clone " + ToolSupport.shellQuote(request.url));
    }

    // ── helpers ──────────────────────────────────────────────

    private ExecutionResponse runInRepo(GitRequestDto request, String gitCommand) throws Exception {
        validatePath(request);
        String path = normalizePath(request.path);
        String command = "cd -- " + ToolSupport.shellQuote(path)
                + " && " + gitCommand;
        return lock.runLocked(() -> executor.execute(command));
    }

    private void validatePath(GitRequestDto request) {
        if (request == null || request.path == null || request.path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
