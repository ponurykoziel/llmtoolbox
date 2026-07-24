package com.sheahorn.llmtoolbox.basics.presets;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.stream.Collectors;

@Startup
@ApplicationScoped
public class PresetDefaults {

    /** Category presets — always available as fallback, never written to DB. */
    public static final Map<String, String> HARDCODED = Map.ofEntries(
            Map.entry("all", "*"),
            Map.entry("fs", "fs_*"),
            Map.entry("net", "net_*"),
            Map.entry("host", "host_*"),
            Map.entry("build", "build_*"),
            Map.entry("mvn", "build_mvn_*"),
            Map.entry("maven", "build_mvn_*"),
            Map.entry("devops", "devops_*"),
            Map.entry("communication", "communication_*"),
            Map.entry("basics", "memory_*,notes_*,clipboard_*"),
            Map.entry("current_time", "time_now"),
            Map.entry("presets", "presets_*"),
            Map.entry("terminal", "terminal_*"),
            Map.entry("calculator", "calculator_*"),
            Map.entry("git", "devops_git_*"),
            Map.entry("docker", "devops_docker_*")
    );

    /** Composite presets — seeded into DB on first boot. */
    private static final Map<String, String> SEED = Map.ofEntries(
            Map.entry("daemon",
                    "fs_files_create,fs_files_delete,fs_files_mkdir,fs_files_move," +
                    "fs_files_overwrite,fs_files_read,fs_files_replace,fs_files_rmdir," +
                    "fs_ls_flat,fs_ls_recursive," +
                    "host_hardware_*,host_netinfo_*,host_sysinfo_*,host_services_*,host_logs_*," +
                    "communication_send_message,clipboard_*,time_now,memory_*,notes_*,net_*"),
            Map.entry("builder",
                    "fs_files_create,fs_files_delete,fs_files_mkdir,fs_files_move," +
                    "fs_files_overwrite,fs_files_read,fs_files_replace,fs_files_rmdir," +
                    "fs_ls_flat,fs_ls_recursive," +
                    "build_mvn_compile,build_mvn_test,build_mvn_test_one"),
            Map.entry("host_ctl", "host_audio_*,host_power_*,host_monitor_*"),
            Map.entry("host_info", "host_hardware_*,host_netinfo_*,host_sysinfo_*,host_services_*,host_logs_*"),
            Map.entry("devops", "devops_*")
    );

    @PostConstruct
    void init() {
        QuarkusTransaction.run(() -> {
            if (Preset.count() > 0) {
                return;
            }

            for (var entry : SEED.entrySet()) {
                Preset preset = new Preset();
                preset.name = entry.getKey();
                preset.prefixes = entry.getValue();
                preset.persist();
            }
        });
    }

    // ── two-tier resolution ──────────────────────────────────

    /** DB first, then hardcoded. Returns null if not found. */
    public static List<String> resolve(String name) {
        Preset db = Preset.byName(name).orElse(null);
        if (db != null) {
            return db.prefixList();
        }
        String hc = HARDCODED.get(name);
        if (hc != null) {
            return List.of(hc.split(",")).stream()
                    .map(String::strip)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return null;
    }

    /** All known preset names: DB ∪ hardcoded. */
    public static Set<String> allNames() {
        Set<String> names = new LinkedHashSet<>(
                Preset.all().stream().map(p -> p.name).collect(Collectors.toSet()));
        names.addAll(HARDCODED.keySet());
        return names;
    }

    /** True if this name exists as a hardcoded default. */
    public static boolean isHardcoded(String name) {
        return HARDCODED.containsKey(name);
    }
}
