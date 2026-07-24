package com.sheahorn.llmtoolbox.buildtools.git;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GitMergeTest {

    @Inject
    GitResource gitResource;

    private Path tmpDir;

    @BeforeEach
    void setUp() throws Exception {
        tmpDir = Files.createTempDirectory(Path.of(System.getProperty("user.dir")), "git-merge-test-");
        run("git init -b main", tmpDir);
        run("git config user.email test@test.com", tmpDir);
        run("git config user.name Test", tmpDir);
        run("git config core.editor true", tmpDir);

        // Create initial commit on main
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nline2\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'initial commit'", tmpDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tmpDir != null && Files.exists(tmpDir)) {
            Files.walk(tmpDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(f -> f.setWritable(true));
            Files.walk(tmpDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ── helpers ──────────────────────────────────────────────

    private String run(String cmd, Path dir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
        pb.directory(dir.toFile());
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes());
        String err = new String(p.getErrorStream().readAllBytes());
        int rc = p.waitFor();
        if (rc != 0) {
            throw new RuntimeException("cmd failed (rc=" + rc + "): " + cmd + "\nstdout: " + out + "\nstderr: " + err);
        }
        return out;
    }

    private String path() {
        return tmpDir.toString();
    }

    private GitBranchRequest branchReq(String branch) {
        GitBranchRequest req = new GitBranchRequest();
        req.path = path();
        req.branch = branch;
        return req;
    }

    private GitPathRequest pathReq() {
        GitPathRequest req = new GitPathRequest();
        req.path = path();
        return req;
    }

    // ── merge_perform_normal ─────────────────────────────────

    @Test
    void testMergePerformNormalSuccess() throws Exception {
        run("git checkout -b feat1", tmpDir);
        Files.writeString(tmpDir.resolve("other.txt"), "feature content");
        run("git add other.txt", tmpDir);
        run("git commit -m 'change on feat1'", tmpDir);
        run("git checkout main", tmpDir);

        var resp = gitResource.mergePerformNormal(branchReq("feat1"));
        assertEquals(0, resp.exitCode, "merge should succeed: " + resp.stdout + " | " + resp.stderr);
    }

    @Test
    void testMergePerformNormalAlreadyUpToDate() throws Exception {
        run("git checkout -b same", tmpDir);
        run("git checkout main", tmpDir);

        var resp = gitResource.mergePerformNormal(branchReq("same"));
        assertEquals(0, resp.exitCode);
        assertTrue(resp.stdout.contains("Already up to date"));
    }

    // ── merge_perform_squash ─────────────────────────────────

    @Test
    void testMergePerformSquashSuccess() throws Exception {
        run("git checkout -b feat2", tmpDir);
        Files.writeString(tmpDir.resolve("other.txt"), "feature content");
        run("git add other.txt", tmpDir);
        run("git commit -m 'change on feat2'", tmpDir);
        run("git checkout main", tmpDir);

        var resp = gitResource.mergePerformSquash(branchReq("feat2"));
        assertEquals(0, resp.exitCode, "squash merge should succeed: " + resp.stdout + " | " + resp.stderr);
    }

    // ── merge conflict + abort ───────────────────────────────

    @Test
    void testMergeConflictAndAbort() throws Exception {
        // Diverge: change same line on both branches
        run("git checkout -b feat3", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nfeature-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'feature change'", tmpDir);

        run("git checkout main", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nmain-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'main change'", tmpDir);

        // Verify divergence
        run("git log --oneline --all", tmpDir);

        // Merge should conflict
        var mergeResp = gitResource.mergePerformNormal(branchReq("feat3"));
        String mergeOutput = mergeResp.stdout + mergeResp.stderr;
        assertTrue(mergeOutput.contains("CONFLICT"),
                "expected CONFLICT in merge output: " + mergeOutput);

        // Abort
        var abortResp = gitResource.mergeAbort(pathReq());
        assertEquals(0, abortResp.exitCode,
                "abort should succeed: " + abortResp.stdout + " | " + abortResp.stderr);

        // Verify we're back to clean state
        var statusResp = gitResource.status(pathReq());
        assertFalse(statusResp.stdout.contains("You have unmerged paths"),
                "should not have unmerged paths after abort: " + statusResp.stdout);
    }

    // ── merge conflict + list conflicts ──────────────────────

    @Test
    void testMergeListConflicts() throws Exception {
        run("git checkout -b feat4", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nfeature-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'feature change'", tmpDir);

        run("git checkout main", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nmain-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'main change'", tmpDir);

        // Merge should conflict
        gitResource.mergePerformNormal(branchReq("feat4"));

        // List conflicts
        var conflictsResp = gitResource.mergeListConflicts(pathReq());
        assertEquals(0, conflictsResp.exitCode);
        assertTrue(conflictsResp.stdout.contains("file.txt"),
                "expected file.txt in conflicts, got: '" + conflictsResp.stdout + "'");
    }

    // ── merge conflict + resolve + continue ──────────────────

    @Test
    void testMergeConflictResolveAndContinue() throws Exception {
        run("git checkout -b feat5", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nfeature-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'feature change'", tmpDir);

        run("git checkout main", tmpDir);
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nmain-change\nline3\n");
        run("git add file.txt", tmpDir);
        run("git commit -m 'main change'", tmpDir);

        // Merge should conflict
        var mergeResp = gitResource.mergePerformNormal(branchReq("feat5"));
        String mergeOutput = mergeResp.stdout + mergeResp.stderr;
        assertTrue(mergeOutput.contains("CONFLICT"),
                "expected CONFLICT in merge output: " + mergeOutput);

        // Resolve conflict
        Files.writeString(tmpDir.resolve("file.txt"), "line1\nresolved\nline3\n");
        run("git add file.txt", tmpDir);

        // Continue
        var continueResp = gitResource.mergeContinue(pathReq());
        assertEquals(0, continueResp.exitCode,
                "merge continue should succeed: " + continueResp.stdout + " | " + continueResp.stderr);

        // Verify clean state
        var statusResp = gitResource.status(pathReq());
        assertFalse(statusResp.stdout.contains("You have unmerged paths"));
    }

    // ── abort when no merge in progress ──────────────────────

    @Test
    void testMergeAbortWhenNoMergeInProgress() throws Exception {
        var resp = gitResource.mergeAbort(pathReq());
        assertNotEquals(0, resp.exitCode,
                "abort with no merge in progress should fail: " + resp.stdout + " | " + resp.stderr);
    }

    // ── continue when no merge in progress ───────────────────

    @Test
    void testMergeContinueWhenNoMergeInProgress() throws Exception {
        var resp = gitResource.mergeContinue(pathReq());
        assertNotEquals(0, resp.exitCode,
                "continue with no merge in progress should fail: " + resp.stdout + " | " + resp.stderr);
    }

    // ── list conflicts when no conflicts ─────────────────────

    @Test
    void testMergeListConflictsWhenNone() throws Exception {
        var resp = gitResource.mergeListConflicts(pathReq());
        assertEquals(0, resp.exitCode);
        assertTrue(resp.stdout.isBlank(),
                "expected empty output when no conflicts, got: '" + resp.stdout + "'");
    }

    // ── push_set_upstream ────────────────────────────────────

    @Test
    void testPushSetUpstreamNoRemote() throws Exception {
        var resp = gitResource.pushSetUpstream(branchReq("main"));
        assertNotEquals(0, resp.exitCode,
                "push with no remote should fail: " + resp.stdout + " | " + resp.stderr);
    }
}
