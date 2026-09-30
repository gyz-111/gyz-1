/* Weather_API 下载站 - 共享脚本（首页与分类下载页共用） */
(function () {
    'use strict';

    // 下载文件相对站点根目录的存放位置（所有页面均位于站点根目录，路径保持一致）
    var DOWNLOAD_DIR = 'weatherall/';

    // MD5 hash of password
    var PASSWORD_HASH = 'c30c8b91097d89a24c00c74b4e15e032';
    var MAX_ATTEMPTS = 4;
    var LOCKOUT_SECONDS = 120;

    var pendingFilename = '';
    var failedAttempts = parseInt(localStorage.getItem('weather_dl_attempts') || '0', 10);
    var lockoutEnd = parseInt(localStorage.getItem('weather_dl_lockout') || '0', 10);
    var modalCloseTimer = null;

    /* ---------- MD5 (RFC 1321, Joseph Myers) ---------- */
    function md5(string) {
        function md5cycle(x, k) {
            var a = x[0], b = x[1], c = x[2], d = x[3];
            a = ff(a, b, c, d, k[0], 7, -680876936); d = ff(d, a, b, c, k[1], 12, -389564586);
            c = ff(c, d, a, b, k[2], 17, 606105819); b = ff(b, c, d, a, k[3], 22, -1044525330);
            a = ff(a, b, c, d, k[4], 7, -176418897); d = ff(d, a, b, c, k[5], 12, 1200080426);
            c = ff(c, d, a, b, k[6], 17, -1473231341); b = ff(b, c, d, a, k[7], 22, -45705983);
            a = ff(a, b, c, d, k[8], 7, 1770035416); d = ff(d, a, b, c, k[9], 12, -1958414417);
            c = ff(c, d, a, b, k[10], 17, -42063); b = ff(b, c, d, a, k[11], 22, -1990404162);
            a = ff(a, b, c, d, k[12], 7, 1804603682); d = ff(d, a, b, c, k[13], 12, -40341101);
            c = ff(c, d, a, b, k[14], 17, -1502002290); b = ff(b, c, d, a, k[15], 22, 1236535329);
            a = gg(a, b, c, d, k[1], 5, -165796510); d = gg(d, a, b, c, k[6], 9, -1069501632);
            c = gg(c, d, a, b, k[11], 14, 643717713); b = gg(b, c, d, a, k[0], 20, -373897302);
            a = gg(a, b, c, d, k[5], 5, -701558691); d = gg(d, a, b, c, k[10], 9, 38016083);
            c = gg(c, d, a, b, k[15], 14, -660478335); b = gg(b, c, d, a, k[4], 20, -405537848);
            a = gg(a, b, c, d, k[9], 5, 568446438); d = gg(d, a, b, c, k[14], 9, -1019803690);
            c = gg(c, d, a, b, k[3], 14, -187363961); b = gg(b, c, d, a, k[8], 20, 1163531501);
            a = gg(a, b, c, d, k[13], 5, -1444681467); d = gg(d, a, b, c, k[2], 9, -51403784);
            c = gg(c, d, a, b, k[7], 14, 1735328473); b = gg(b, c, d, a, k[12], 20, -1926607734);
            a = hh(a, b, c, d, k[5], 4, -378558); d = hh(d, a, b, c, k[8], 11, -2022574463);
            c = hh(c, d, a, b, k[11], 16, 1839030562); b = hh(b, c, d, a, k[14], 23, -35309556);
            a = hh(a, b, c, d, k[1], 4, -1530992060); d = hh(d, a, b, c, k[4], 11, 1272893353);
            c = hh(c, d, a, b, k[7], 16, -155497632); b = hh(b, c, d, a, k[10], 23, -1094730640);
            a = hh(a, b, c, d, k[13], 4, 681279174); d = hh(d, a, b, c, k[0], 11, -358537222);
            c = hh(c, d, a, b, k[3], 16, -722521979); b = hh(b, c, d, a, k[6], 23, 76029189);
            a = hh(a, b, c, d, k[9], 4, -640364487); d = hh(d, a, b, c, k[12], 11, -421815835);
            c = hh(c, d, a, b, k[15], 16, 530742520); b = hh(b, c, d, a, k[2], 23, -995338651);
            a = ii(a, b, c, d, k[0], 6, -198630844); d = ii(d, a, b, c, k[7], 10, 1126891415);
            c = ii(c, d, a, b, k[14], 15, -1416354905); b = ii(b, c, d, a, k[5], 21, -57434055);
            a = ii(a, b, c, d, k[12], 6, 1700485571); d = ii(d, a, b, c, k[3], 10, -1894986606);
            c = ii(c, d, a, b, k[10], 15, -1051523); b = ii(b, c, d, a, k[1], 21, -2054922799);
            a = ii(a, b, c, d, k[8], 6, 1873313359); d = ii(d, a, b, c, k[15], 10, -30611744);
            c = ii(c, d, a, b, k[6], 15, -1560198380); b = ii(b, c, d, a, k[13], 21, 1309151649);
            a = ii(a, b, c, d, k[4], 6, -145523070); d = ii(d, a, b, c, k[11], 10, -1120210379);
            c = ii(c, d, a, b, k[2], 15, 718787259); b = ii(b, c, d, a, k[9], 21, -343485551);
            x[0] = add32(a, x[0]); x[1] = add32(b, x[1]); x[2] = add32(c, x[2]); x[3] = add32(d, x[3]);
        }
        function cmn(q, a, b, x, s, t) { a = add32(add32(a, q), add32(x, t)); return add32((a << s) | (a >>> (32 - s)), b); }
        function ff(a, b, c, d, x, s, t) { return cmn((b & c) | ((~b) & d), a, b, x, s, t); }
        function gg(a, b, c, d, x, s, t) { return cmn((b & d) | (c & (~d)), a, b, x, s, t); }
        function hh(a, b, c, d, x, s, t) { return cmn(b ^ c ^ d, a, b, x, s, t); }
        function ii(a, b, c, d, x, s, t) { return cmn(c ^ (b | (~d)), a, b, x, s, t); }
        function md51(s) {
            var n = s.length, state = [1732584193, -271733879, -1732584194, 271733878], i;
            for (i = 64; i <= n; i += 64) { md5cycle(state, md5blk(s.substring(i - 64, i))); }
            s = s.substring(i - 64);
            var tail = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
            for (i = 0; i < s.length; i++) { tail[i >> 2] |= s.charCodeAt(i) << ((i % 4) << 3); }
            tail[i >> 2] |= 0x80 << ((i % 4) << 3);
            if (i > 55) { md5cycle(state, tail); for (i = 0; i < 16; i++) { tail[i] = 0; } }
            tail[14] = n * 8;
            md5cycle(state, tail);
            return state;
        }
        function md5blk(s) {
            var md5blks = [], i;
            for (i = 0; i < 64; i += 4) {
                md5blks[i >> 2] = s.charCodeAt(i) + (s.charCodeAt(i + 2) << 8) + (s.charCodeAt(i + 1) << 16) + (s.charCodeAt(i + 3) << 24);
            }
            return md5blks;
        }
        var hex_chr = '0123456789abcdef'.split('');
        function rhex(n) {
            var s = '', j = 0;
            for (; j < 4; j++) { s += hex_chr[(n >> (j * 8 + 4)) & 0x0F] + hex_chr[(n >> (j * 8)) & 0x0F]; }
            return s;
        }
        function hex(x) { for (var i = 0; i < x.length; i++) { x[i] = rhex(x[i]); } return x.join(''); }
        function add32(a, b) { return (a + b) & 0xFFFFFFFF; }
        return hex(md51(string));
    }

    /* ---------- 锁定与下载流程 ---------- */
    function getRemainingLockout() {
        if (lockoutEnd === 0) { return 0; }
        return Math.max(0, Math.ceil((lockoutEnd - Date.now()) / 1000));
    }

    function checkLockout() {
        var remaining = getRemainingLockout();
        if (remaining > 0) { return remaining; }
        if (lockoutEnd > 0) {
            failedAttempts = 0;
            lockoutEnd = 0;
            localStorage.setItem('weather_dl_attempts', '0');
            localStorage.setItem('weather_dl_lockout', '0');
        }
        return 0;
    }

    function startDownload(filename) {
        // encodeURI 保留路径分隔符，仅对中文/空格等做百分号编码，兼容 Netlify 等静态托管
        window.location.href = DOWNLOAD_DIR + encodeURI(filename);
    }

    window.requestDownload = function (filename) {
        // 特供版无需密码
        if (filename.indexOf('特供版') !== -1) {
            startDownload(filename);
            return;
        }
        pendingFilename = filename;
        var modal = document.getElementById('passwordModal');
        if (!modal) { startDownload(filename); return; }
        if (modalCloseTimer) { clearTimeout(modalCloseTimer); modalCloseTimer = null; }
        modal.classList.remove('closing');
        modal.classList.add('active');
        document.getElementById('modalFileName').textContent = '正在下载: ' + filename;
        document.getElementById('passwordInput').value = '';
        document.getElementById('modalError').textContent = '';
        document.getElementById('modalLockout').textContent = '';
        setTimeout(function () { document.getElementById('passwordInput').focus(); }, 100);
    };

    window.closeModal = function () {
        var modal = document.getElementById('passwordModal');
        if (!modal.classList.contains('active')) { pendingFilename = ''; return; }
        modal.classList.add('closing');
        if (modalCloseTimer) { clearTimeout(modalCloseTimer); }
        modalCloseTimer = setTimeout(function () {
            modal.classList.remove('active', 'closing');
            pendingFilename = '';
            modalCloseTimer = null;
        }, 250);
    };

    window.confirmPassword = function () {
        var input = document.getElementById('passwordInput').value;
        var errorEl = document.getElementById('modalError');
        var lockoutEl = document.getElementById('modalLockout');
        var remaining = checkLockout();

        if (remaining > 0) {
            lockoutEl.textContent = '输入错误次数过多，请等待 ' + remaining + ' 秒后再试';
            return;
        }

        if (md5(input) === PASSWORD_HASH) {
            failedAttempts = 0;
            lockoutEnd = 0;
            localStorage.setItem('weather_dl_attempts', '0');
            localStorage.setItem('weather_dl_lockout', '0');
            errorEl.textContent = '';
            window.closeModal();
            startDownload(pendingFilename);
        } else {
            failedAttempts++;
            localStorage.setItem('weather_dl_attempts', String(failedAttempts));
            if (failedAttempts >= MAX_ATTEMPTS) {
                lockoutEnd = Date.now() + LOCKOUT_SECONDS * 1000;
                localStorage.setItem('weather_dl_lockout', String(lockoutEnd));
                lockoutEl.textContent = '密码错误次数过多，已锁定 ' + LOCKOUT_SECONDS + ' 秒';
            } else {
                errorEl.textContent = '密码错误，剩余尝试次数: ' + (MAX_ATTEMPTS - failedAttempts);
            }
            document.getElementById('passwordInput').value = '';
        }
    };

    /* ---------- 滚动渐入 ---------- */
    function animateOnScroll() {
        var elements = document.querySelectorAll('.feature-card, .version-item, .step, .download-option, .type-card');
        elements.forEach(function (element) {
            var rect = element.getBoundingClientRect();
            if (rect.top < window.innerHeight * 0.88 && rect.bottom > 0) {
                element.classList.add('animate');
            } else if (rect.bottom <= 0 || rect.top >= window.innerHeight) {
                element.classList.remove('animate');
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var pwdInput = document.getElementById('passwordInput');
        if (pwdInput) {
            pwdInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') { window.confirmPassword(); }
            });
        }
        var modal = document.getElementById('passwordModal');
        if (modal) {
            modal.addEventListener('click', function (e) {
                if (e.target === this) { window.closeModal(); }
            });
        }
        setInterval(function () {
            var lockoutEl = document.getElementById('modalLockout');
            if (lockoutEl && lockoutEl.textContent.length > 0) {
                var remaining = checkLockout();
                lockoutEl.textContent = remaining > 0
                    ? '密码错误次数过多，请等待 ' + remaining + ' 秒后再试'
                    : '';
            }
        }, 1000);

        checkLockout();
        animateOnScroll();
        window.addEventListener('scroll', animateOnScroll, { passive: true });
        window.addEventListener('resize', animateOnScroll);
    });
})();

/* Weather_API 下载站 - 页面增强：版本搜索 + 返回顶部 */
(function () {
    'use strict';

    /* ---------- 版本搜索（存在搜索框时启用） ---------- */
    var searchInput = document.querySelector('.version-search input');
    var items = Array.prototype.slice.call(document.querySelectorAll('.versions-list .version-item'));
    var noResult = document.querySelector('.no-result');
    if (searchInput && items.length) {
        searchInput.addEventListener('input', function () {
            var kw = searchInput.value.trim().toLowerCase();
            var visible = 0;
            items.forEach(function (item) {
                var text = (item.getAttribute('data-keywords') || '') + ' ' + item.textContent;
                var hit = !kw || text.toLowerCase().indexOf(kw) !== -1;
                item.style.display = hit ? '' : 'none';
                if (hit) { visible++; item.classList.add('animate'); }
            });
            if (noResult) { noResult.classList.toggle('show', visible === 0); }
        });
    }

    /* ---------- 返回顶部（存在版本列表时启用） ---------- */
    if (document.querySelector('.versions-list')) {
        var btn = document.createElement('button');
        btn.className = 'back-top';
        btn.type = 'button';
        btn.setAttribute('aria-label', '返回顶部');
        btn.innerHTML = '&uarr;';
        btn.addEventListener('click', function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
        document.body.appendChild(btn);
        var toggle = function () { btn.classList.toggle('show', window.scrollY > 400); };
        window.addEventListener('scroll', toggle, { passive: true });
        toggle();
    }
})();
