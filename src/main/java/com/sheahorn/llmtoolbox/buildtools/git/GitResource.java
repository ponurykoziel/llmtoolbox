package com.sheahorn.llmtoolbox.buildtools.git;

import com.sheahorn.llmtoolbox.buildtools.LogToolCall;
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
@LogToolCall
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
    public ExecutionResponse status(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git status");
    }

    @Operation(
            operationId = "devops_git_log_recent",
            summary = "Runs `git log --oneline -n <n>` inside the repo path — n is required"
    )
    @POST
    @Path("/log-recent")
    public ExecutionResponse logRecent(GitLogRecentRequest request) throws Exception {
        validatePath(request.path);
        if (request.n == null || request.n <= 0) {
            throw new IllegalArgumentException("n is required and must be positive");
        }
        return runInRepo(request.path, "git log --oneline -n " + request.n);
    }

    @Operation(
            operationId = "devops_git_log_oneline",
            summary = "Runs `git log --oneline` (full history, no limit) inside the repo path"
    )
    @POST
    @Path("/log-oneline")
    public ExecutionResponse logOneline(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git log --oneline");
    }

    @Operation(
            operationId = "devops_git_log",
            summary = "Runs `git log` (full log, default format) inside the repo path"
    )
    @POST
    @Path("/log")
    public ExecutionResponse log(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git log");
    }

    @Operation(
            operationId = "devops_git_diff",
            summary = "Runs `git diff` inside the repo path"
    )
    @POST
    @Path("/diff")
    public ExecutionResponse diff(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git diff");
    }

    @Operation(
            operationId = "devops_git_diff_staged",
            summary = "Runs `git diff --staged` inside the repo path"
    )
    @POST
    @Path("/diff-staged")
    public ExecutionResponse diffStaged(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git diff --staged");
    }

    // ── info ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_print_remote",
            summary = "Runs `git remote -v` to show remote names and URLs"
    )
    @POST
    @Path("/print-remote")
    public ExecutionResponse printRemote(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git remote -v");
    }

    @Operation(
            operationId = "devops_git_print_config",
            summary = "Runs `git config --list` to show local repo configuration"
    )
    @POST
    @Path("/print-config")
    public ExecutionResponse printConfig(GitPathRequest request) throws Exception {
        return runInRepo(request.path, "git config --list");
    }

    // ── staging / committing ─────────────────────────────────

    @Operation(
            operationId = "devops_git_add",
            summary = "Runs `git add <files>` inside the repo path — files is required"
    )
    @POST
    @Path("/add")
    public ExecutionResponse add(GitAddRequest request) throws Exception {
        validatePath(request.path);
        if (request.files == null || request.files.isBlank()) {
            throw new IllegalArgumentException("files is required");
        }
        return runInRepo(request.path, "git add -- " + ToolSupport.shellQuote(request.files));
    }

    @Operation(
            operationId = "devops_git_commit",
            summary = "Runs `git commit -m <message>` inside the repo path"
    )
    @POST
    @Path("/commit")
    public ExecutionResponse commit(GitCommitRequest request) throws Exception {
        validatePath(request.path);
        if (request.message == null || request.message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        return runInRepo(request.path, "git commit -m " + ToolSupport.shellQuote(request.message));
    }

    // ── reset / clean ────────────────────────────────────────

    @Operation(
            operationId = "devops_git_reset_mixed",
            summary = "Runs `git reset` (mixed reset to HEAD)"
    )
    @POST
    @Path("/reset-mixed")
    public ExecutionResponse resetMixed(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git reset");
    }

    @Operation(
            operationId = "devops_git_reset_soft",
            summary = "Runs `git reset --soft` (soft reset to HEAD)"
    )
    @POST
    @Path("/reset-soft")
    public ExecutionResponse resetSoft(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git reset --soft");
    }

    @Operation(
            operationId = "devops_git_reset_hard",
            summary = "Runs `git reset --hard` (hard reset to HEAD). DESTRUCTIVE — discards working tree changes."
    )
    @POST
    @Path("/reset-hard")
    public ExecutionResponse resetHard(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git reset --hard");
    }

    @Operation(
            operationId = "devops_git_reset_hard_origin",
            summary = "Runs `git fetch origin <branch> && git reset --hard origin/<branch>`. DESTRUCTIVE. Requires confirm=true."
    )
    @POST
    @Path("/reset-hard-origin")
    public ExecutionResponse resetHardOrigin(GitResetHardOriginRequest request) throws Exception {
        validatePath(request.path);
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
    public ExecutionResponse clean(GitPathRequest request) throws Exception {
        validatePath(request.path);
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
    public ExecutionResponse branchListLocal(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git branch");
    }

    @Operation(
            operationId = "devops_git_branch_list_remote",
            summary = "Runs `git branch -r` to list remote-tracking branches"
    )
    @POST
    @Path("/branch-list-remote")
    public ExecutionResponse branchListRemote(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git branch -r");
    }

    @Operation(
            operationId = "devops_git_branch_create",
            summary = "Runs `git branch <name>` to create a new branch"
    )
    @POST
    @Path("/branch-create")
    public ExecutionResponse branchCreate(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git branch " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_delete_if_merged",
            summary = "Runs `git branch -d <branch>` (safe delete — refuses if not merged)"
    )
    @POST
    @Path("/branch-delete-if-merged")
    public ExecutionResponse branchDeleteIfMerged(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git branch -d " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_delete_force",
            summary = "Runs `git branch -D <branch>` (force delete — even if not merged)"
    )
    @POST
    @Path("/branch-delete-force")
    public ExecutionResponse branchDeleteForce(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git branch -D " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_branch_set_upstream",
            summary = "Runs `git branch --set-upstream-to=<remote>/<remoteBranch> <branch>`"
    )
    @POST
    @Path("/branch-set-upstream")
    public ExecutionResponse branchSetUpstream(GitBranchSetUpstreamRequest request) throws Exception {
        validatePath(request.path);
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
        return runInRepo(request.path, cmd);
    }

    // ── checkout ─────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_checkout_branch",
            summary = "Runs `git checkout <branch>` to switch to a branch (HEAD)"
    )
    @POST
    @Path("/checkout-branch")
    public ExecutionResponse checkoutBranch(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git checkout " + ToolSupport.shellQuote(request.branch));
    }

    @Operation(
            operationId = "devops_git_checkout_commit",
            summary = "Runs `git checkout <commit>` to detach HEAD at a specific commit"
    )
    @POST
    @Path("/checkout-commit")
    public ExecutionResponse checkoutCommit(GitCheckoutCommitRequest request) throws Exception {
        validatePath(request.path);
        if (request.commit == null || request.commit.isBlank()) {
            throw new IllegalArgumentException("commit is required");
        }
        return runInRepo(request.path, "git checkout " + ToolSupport.shellQuote(request.commit));
    }

    @Operation(
            operationId = "devops_git_checkout_new_branch",
            summary = "Runs `git checkout -b <branch>` to create and switch to a new branch"
    )
    @POST
    @Path("/checkout-new-branch")
    public ExecutionResponse checkoutNewBranch(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git checkout -b " + ToolSupport.shellQuote(request.branch));
    }

    // ── merge ────────────────────────────────────────────────
    // MAIN VERSION — conflicting

    @Operation(
            operationId = "devops_git_merge",
            summary = "Runs `git merge <branch>` inside the repo path"
    )
    @POST
    @Path("/merge")
    public ExecutionResponse merge(GitBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }
        return runInRepo(request.path, "git merge " + ToolSupport.shellQuote(request.branch));
    }

    // ── fetch ────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_fetch",
            summary = "Runs `git fetch` inside the repo path"
    )
    @POST
    @Path("/fetch")
    public ExecutionResponse fetch(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git fetch");
    }

    // ── push ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_push",
            summary = "Runs `git push` inside the repo path"
    )
    @POST
    @Path("/push")
    public ExecutionResponse push(GitPathRequest request) throws Exception {
        validatePath(request.path);
        return runInRepo(request.path, "git push");
    }

    @Operation(
            operationId = "devops_git_push_custom_branch",
            summary = "Runs `git push <remote> <branch>` or `git push <remote> <branch>:<remoteBranch>` if remoteBranch is set — remote and branch are required"
    )
    @POST
    @Path("/push-custom-branch")
    public ExecutionResponse pushCustomBranch(GitPushCustomBranchRequest request) throws Exception {
        validatePath(request.path);
        if (request.remote == null || request.remote.isBlank()) {
            throw new IllegalArgumentException("remote is required");
        }
        if (request.branch == null || request.branch.isBlank()) {
            throw new IllegalArgumentException("branch is required");
        }

        String cmd = "git push " + ToolSupport.shellQuote(request.remote);
        if (request.remoteBranch != null && !request.remoteBranch.isBlank()) {
            cmd += " " + ToolSupport.shellQuote(request.branch) + ":" + ToolSupport.shellQuote(request.remoteBranch);
        } else {
            cmd += " " + ToolSupport.shellQuote(request.branch);
        }
        return runInRepo(request.path, cmd);
    }

    // ── pull ─────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_pull",
            summary = "Runs `git pull` inside the repo path (default merge)"
    )
    @POST
    @Path("/pull")
    public ExecutionResponse pull(GitPullRequest request) throws Exception {
        return doPull(request, "");
    }

    @Operation(
            operationId = "devops_git_pull_rebase",
            summary = "Runs `git pull --rebase` inside the repo path"
    )
    @POST
    @Path("/pull-rebase")
    public ExecutionResponse pullRebase(GitPullRequest request) throws Exception {
        return doPull(request, "--rebase");
    }

    @Operation(
            operationId = "devops_git_pull_ff",
            summary = "Runs `git pull --ff-only` inside the repo path"
    )
    @POST
    @Path("/pull-ff")
    public ExecutionResponse pullFf(GitPullRequest request) throws Exception {
        return doPull(request, "--ff-only");
    }

    private ExecutionResponse doPull(GitPullRequest request, String flag) throws Exception {
        validatePath(request.path);
        String branch = (request.branch != null && !request.branch.isBlank())
                ? request.branch : "";

        StringBuilder cmd = new StringBuilder("git pull");
        if (!flag.isEmpty()) {
            cmd.append(" ").append(flag);
        }
        if (!branch.isEmpty()) {
            cmd.append(" ").append(ToolSupport.shellQuote(branch));
        }
        return runInRepo(request.path, cmd.toString());
    }

    // ── clone ────────────────────────────────────────────────

    @Operation(
            operationId = "devops_git_clone",
            summary = "Runs `git clone <url>` — path is the parent directory to clone into"
    )
    @POST
    @Path("/clone")
    public ExecutionResponse clone(GitCloneRequest request) throws Exception {
        validatePath(request.path);
        if (request.url == null || request.url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }
        return runInRepo(request.path, "git clone " + ToolSupport.shellQuote(request.url));
    }

    // ── helpers ──────────────────────────────────────────────

    private ExecutionResponse runInRepo(String rawPath, String gitCommand) throws Exception {
        String path = normalizePath(rawPath);
        String command = "cd -- " + ToolSupport.shellQuote(path)
                + " && " + gitCommand;
        return lock.runLocked(() -> executor.execute(command));
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
    }
}
