"""
Stealth patches for headless Chromium.

Detection vectors and their mitigations, ordered by how commonly
sites check them. We inject this as an init script on every page.

Reference: puppeteer-extra-plugin-stealth, fingerprinting research.
"""

STEALTH_INIT_SCRIPT = r"""
// ── 1. navigator.webdriver ── (most common check)
Object.defineProperty(navigator, 'webdriver', { get: () => false });

// ── 2. window.chrome ── (real Chrome has this)
if (!window.chrome) {
    window.chrome = { runtime: {}, loadTimes: function() {}, csi: function() {} };
}
if (!window.chrome.runtime) {
    window.chrome.runtime = {};
}

// ── 3. navigator.plugins ── (headless = empty array)
const fakePlugins = [
    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: '' },
    { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' },
];
Object.defineProperty(navigator, 'plugins', {
    get: () => {
        const arr = fakePlugins.map(p => {
            const plugin = Object.create(Plugin.prototype);
            Object.defineProperty(plugin, 'name', { value: p.name });
            Object.defineProperty(plugin, 'filename', { value: p.filename });
            Object.defineProperty(plugin, 'description', { value: p.description });
            Object.defineProperty(plugin, 'length', { value: 1 });
            // MimeType entry
            const mime = Object.create(MimeType.prototype);
            Object.defineProperty(mime, 'type', { value: 'application/pdf' });
            Object.defineProperty(mime, 'suffixes', { value: 'pdf' });
            Object.defineProperty(plugin, 'item', { value: () => mime });
            Object.defineProperty(plugin, 'namedItem', { value: () => mime });
            return plugin;
        });
        Object.defineProperty(arr, 'item', { value: (i) => arr[i] });
        Object.defineProperty(arr, 'namedItem', { value: (name) => arr.find(p => p.name === name) });
        Object.defineProperty(arr, 'refresh', { value: () => {} });
        return arr;
    }
});

// ── 4. navigator.mimeTypes ──
Object.defineProperty(navigator, 'mimeTypes', {
    get: () => {
        const types = [
            { type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' },
            { type: 'text/pdf', suffixes: 'pdf', description: 'Portable Document Format' },
        ];
        const arr = types.map(t => {
            const m = Object.create(MimeType.prototype);
            Object.defineProperty(m, 'type', { value: t.type });
            Object.defineProperty(m, 'suffixes', { value: t.suffixes });
            Object.defineProperty(m, 'description', { value: t.description });
            return m;
        });
        Object.defineProperty(arr, 'item', { value: (i) => arr[i] });
        Object.defineProperty(arr, 'namedItem', { value: (name) => arr.find(m => m.type === name) });
        return arr;
    }
});

// ── 5. navigator.platform ── (should match UA)
Object.defineProperty(navigator, 'platform', { get: () => 'Win32' });

// ── 6. navigator.hardwareConcurrency ──
Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8 });

// ── 7. navigator.deviceMemory ──
Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });

// ── 8. navigator.languages ──
Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });

// ── 9. Permissions API ── (headless behaves differently)
const origQuery = window.navigator.permissions.query.bind(window.navigator.permissions);
window.navigator.permissions.query = (parameters) => {
    if (parameters.name === 'notifications') {
        return Promise.resolve({ state: Notification.permission, onchange: null });
    }
    return origQuery(parameters);
};

// ── 10. Notification permission ──
Object.defineProperty(Notification, 'permission', { get: () => 'default' });

// ── 11. Screen dimensions ── (outer should be > inner = browser chrome)
Object.defineProperty(window, 'outerWidth', { get: () => 1280 });
Object.defineProperty(window, 'outerHeight', { get: () => 800 });
// innerWidth/Height are set by viewport, leave them

// ── 12. screen.availWidth/Height ──
Object.defineProperty(screen, 'availWidth', { get: () => 1280 });
Object.defineProperty(screen, 'availHeight', { get: () => 770 });

// ── 13. WebGL vendor/renderer ── (headless = Google SwiftShader)
const getParameterProxy = (orig) => {
    return function(parameter) {
        if (parameter === 37445) { return 'Intel Inc.'; }        // UNMASKED_VENDOR_WEBGL
        if (parameter === 37446) { return 'Intel Iris OpenGL Engine'; } // UNMASKED_RENDERER_WEBGL
        return orig.call(this, parameter);
    };
};

const patchWebGL = (canvas) => {
    try {
        const ctx = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
        if (ctx) {
            ctx.getParameter = getParameterProxy(ctx.getParameter);
        }
    } catch(e) {}
};

// Patch existing canvases
document.querySelectorAll('canvas').forEach(patchWebGL);

// Patch future canvases
const origGetContext = HTMLCanvasElement.prototype.getContext;
HTMLCanvasElement.prototype.getContext = function(...args) {
    const ctx = origGetContext.apply(this, args);
    if (args[0] && args[0].includes('webgl')) {
        if (ctx) {
            ctx.getParameter = getParameterProxy(ctx.getParameter);
        }
    }
    return ctx;
};

// ── 14. navigator.connection ──
if (!navigator.connection) {
    Object.defineProperty(navigator, 'connection', {
        get: () => ({
            effectiveType: '4g',
            rtt: 50,
            downlink: 10,
            saveData: false,
            onchange: null,
        })
    });
}

// ── 15. navigator.mediaDevices ──
if (!navigator.mediaDevices) {
    Object.defineProperty(navigator, 'mediaDevices', {
        get: () => ({
            enumerateDevices: () => Promise.resolve([]),
            getUserMedia: () => Promise.reject(new Error('NotAllowedError')),
        })
    });
}

// ── 16. Error stack traces ── (strip automation frames)
// Done via --disable-blink-features=AutomationControlled at launch

// ── 17. document.hidden / visibilityState ──
Object.defineProperty(document, 'hidden', { get: () => false });
Object.defineProperty(document, 'visibilityState', { get: () => 'visible' });

// ── 18. navigator.vendor ──
Object.defineProperty(navigator, 'vendor', { get: () => 'Google Inc.' });

// ── 19. navigator.productSub ──
Object.defineProperty(navigator, 'productSub', { get: () => '20030107' });

// ── 20. navigator.appVersion ── (should include the UA)
Object.defineProperty(navigator, 'appVersion', {
    get: () => '5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
});
"""


# ── Context-level stealth config ───────────────────────────────────────────

STEALTH_CONTEXT_CONFIG = {
    "user_agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "viewport": {"width": 1280, "height": 720},
    "locale": "en-US",
    "timezone_id": "America/New_York",
    "geolocation": {"latitude": 40.7128, "longitude": -74.0060},
    "permissions": ["geolocation"],
    "color_scheme": "light",
    "device_scale_factor": 1,
    "is_mobile": False,
    "has_touch": False,
    "extra_http_headers": {
        "Accept-Language": "en-US,en;q=0.9",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Encoding": "gzip, deflate, br",
        "Sec-Ch-Ua": '"Chromium";v="124", "Google Chrome";v="124", "Not-A.Brand";v="99"',
        "Sec-Ch-Ua-Mobile": "?0",
        "Sec-Ch-Ua-Platform": '"Windows"',
    },
}


# ── Browser launch args ─────────────────────────────────────────────────────

STEALTH_LAUNCH_ARGS = [
    "--no-sandbox",
    "--disable-blink-features=AutomationControlled",
    "--disable-features=IsolateOrigins,site-per-process",
    "--disable-dev-shm-usage",
    "--disable-infobars",
    "--disable-breakpad",
    "--disable-component-extensions-with-background-pages",
    "--disable-default-apps",
    "--disable-extensions",
    "--disable-hang-monitor",
    "--disable-prompt-on-repost",
    "--disable-sync",
    "--disable-translate",
    "--metrics-recording-only",
    "--no-first-run",
    "--password-store=basic",
    "--use-mock-keychain",
    "--enable-features=NetworkService,NetworkServiceInProcess",
]
