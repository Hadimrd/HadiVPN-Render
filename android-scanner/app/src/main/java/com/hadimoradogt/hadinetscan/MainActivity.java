package com.hadimoradogt.hadinetscan;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(10, 17, 31);
    private static final int PANEL = Color.rgb(19, 30, 48);
    private static final int TEXT = Color.rgb(238, 245, 255);
    private static final int MUTED = Color.rgb(155, 174, 198);
    private static final int BLUE = Color.rgb(72, 157, 255);
    private static final int GREEN = Color.rgb(87, 220, 166);

    private EditText hostInput;
    private EditText hostHeaderInput;
    private EditText pathInput;
    private EditText portsInput;
    private Spinner countSpinner;
    private TextView statusText;
    private TextView resultsText;
    private TextView progressText;
    private ProgressBar progressBar;
    private Button startButton;
    private Button stopButton;
    private Button copyButton;
    private volatile boolean stopScan = false;
    private volatile ExecutorService scanExecutor;
    private String lastCopy = "";
    private String lastHost = "www.cloudflare.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(22), dp(18), dp(28));
        scroll.addView(page);

        TextView brand = text("⌁  Hadi NetCheck", 25, TEXT, true);
        page.addView(brand);
        TextView subtitle = text("اسکن IP کلادفلر از روی همین گوشی", 14, MUTED, false);
        subtitle.setPadding(0, dp(5), 0, dp(18));
        page.addView(subtitle);

        LinearLayout intro = card();
        intro.addView(text("تست واقعی اتصال TCP + TLS", 19, TEXT, true));
        intro.addView(text("هر IP مستقیماً از اینترنت فعلی گوشی آزمایش می‌شود. زمان از شروع اتصال TCP تا پایان TLS اندازه‌گیری می‌شود؛ این پینگ ICMP نیست، اما برای بررسی مسیر HTTPS/TLS کاربردی‌تر است.", 13, MUTED, false));
        intro.addView(text("برای سنجش اینترنت اپراتور خودت، VPN یا پراکسی فعلی را خاموش کن. نتیجه‌ها با SNI واردشده معتبرند؛ یک IP برای همه دامنه‌ها لزوماً «تمیز» نیست.", 12, Color.rgb(255, 207, 121), false));
        page.addView(intro);

        LinearLayout settings = card();
        settings.addView(text("SNI / دامنه TLS کانفیگ", 14, TEXT, true));
        hostInput = edit("your-domain.example");
        settings.addView(hostInput);
        settings.addView(text("همان Server Name یا SNI را از کانفیگت کپی کن؛ دامنهٔ نمونه را با دامنهٔ واقعی خودت عوض کن.", 11, MUTED, false));

        settings.addView(text("Host header (اختیاری)", 14, TEXT, true));
        hostHeaderInput = edit("");
        settings.addView(hostHeaderInput);
        settings.addView(text("اگر Host در کانفیگ با SNI یکی است خالی بگذار؛ در غیر این صورت Host را جدا وارد کن.", 11, MUTED, false));

        settings.addView(text("مسیر WebSocket", 14, TEXT, true));
        pathInput = edit("/ws");
        settings.addView(pathInput);
        settings.addView(text("برای کانفیگ VLESS + WebSocket + TLS همین مسیر باید با path کانفیگ برابر باشد. این نسخه gRPC یا TCP خام را بررسی نمی‌کند.", 11, MUTED, false));

        settings.addView(text("تعداد نمونه IP", 14, TEXT, true));
        countSpinner = new Spinner(this);
        String[] counts = {"100", "250", "500"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, counts);
        countSpinner.setAdapter(adapter);
        countSpinner.setSelection(1);
        settings.addView(countSpinner);

        settings.addView(text("پورت‌های TLS", 14, TEXT, true));
        portsInput = edit("443,2053,8443");
        settings.addView(portsInput);
        settings.addView(text("پورت‌های امن رایج Cloudflare: 443, 2053, 2083, 2087, 2096, 8443", 11, MUTED, false));
        page.addView(settings);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        startButton = button("شروع اسکن", BLUE);
        stopButton = button("توقف", Color.rgb(100, 111, 132));
        stopButton.setEnabled(false);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(50), 1);
        bp.setMargins(0, 0, dp(8), 0);
        actions.addView(startButton, bp);
        actions.addView(stopButton, new LinearLayout.LayoutParams(0, dp(50), 1));
        page.addView(actions);

        LinearLayout statusCard = card();
        statusText = text("آماده. اسکن از اتصال فعلی گوشی انجام می‌شود.", 13, MUTED, false);
        statusCard.addView(statusText);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(7));
        pp.setMargins(0, dp(13), 0, dp(7));
        statusCard.addView(progressBar, pp);
        progressText = text("هنوز تستی انجام نشده", 11, MUTED, false);
        statusCard.addView(progressText);
        page.addView(statusCard);

        LinearLayout resultCard = card();
        resultCard.addView(text("بهترین IPهای قابل اتصال", 18, TEXT, true));
        resultCard.addView(text("فقط اتصال‌هایی نمایش داده می‌شوند که TLS و گواهی دامنه با موفقیت تأیید شده باشند. ۲۰ نتیجه برتر چند بار دوباره تست می‌شوند.", 12, MUTED, false));
        resultsText = text("بعد از شروع اسکن، نتایج واقعی این شبکه اینجا می‌آیند.", 13, MUTED, false);
        resultsText.setTypeface(Typeface.MONOSPACE);
        resultsText.setPadding(0, dp(12), 0, dp(10));
        resultCard.addView(resultsText);
        copyButton = button("کپی IPهای پیدا‌شده", GREEN);
        copyButton.setEnabled(false);
        resultCard.addView(copyButton);
        page.addView(resultCard);

        TextView footer = text("نکته: این برنامه IPهای رسمی بازه‌های Cloudflare را نمونه‌گیری می‌کند. تست واقعی روی گوشی انجام می‌شود و نتیجه برای شبکه، زمان و SNI فعلی است؛ ممکن است با تغییر اپراتور یا دامنه عوض شود. برنامه تاریخچه یا IP شما را به هیچ سرور شخصی ارسال نمی‌کند.", 11, MUTED, false);
        footer.setPadding(dp(4), dp(12), dp(4), 0);
        page.addView(footer);

        setContentView(scroll);
        startButton.setOnClickListener(v -> startScan());
        stopButton.setOnClickListener(v -> cancelScan());
        copyButton.setOnClickListener(v -> copyResults());
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(15), dp(16), dp(15));
        box.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(12));
        box.setLayoutParams(p);
        return box;
    }

    private TextView text(String s, float size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setLineSpacing(dp(3), 1.0f);
        v.setPadding(0, dp(3), 0, dp(6));
        return v;
    }

    private EditText edit(String value) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setText(value);
        e.setTextSize(14);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setSelectAllOnFocus(true);
        e.setPadding(dp(11), dp(4), dp(11), dp(4));
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        p.setMargins(0, dp(4), 0, dp(6));
        e.setLayoutParams(p);
        return e;
    }

    private Button button(String title, int color) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        return b;
    }

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    private void startScan() {
        final String rawHost = hostInput.getText().toString().trim();
        final String rawHeader = hostHeaderInput.getText().toString().trim();
        final String rawPath = pathInput.getText().toString().trim();
        final String host;
        final String headerHost;
        final String wsPath;
        final List<Integer> ports;
        try {
            if (rawHost.isEmpty() || rawHost.contains("://") || rawHost.contains("/") || rawHost.contains(":") || rawHost.contains(" ")) {
                throw new IllegalArgumentException("SNI را فقط به‌شکل دامنه وارد کن.");
            }
            host = IDN.toASCII(rawHost, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
            if (host.length() > 253 || !host.contains(".") || host.startsWith(".") || host.endsWith(".")) {
                throw new IllegalArgumentException("دامنه SNI معتبر نیست.");
            }
            String candidateHeader = rawHeader.isEmpty() ? host : rawHeader;
            if (candidateHeader.contains("://") || candidateHeader.contains("/") || candidateHeader.contains(":") ||
                candidateHeader.contains(" ") || candidateHeader.indexOf(13) >= 0 || candidateHeader.indexOf(10) >= 0) {
                throw new IllegalArgumentException("Host header را فقط به‌شکل دامنه وارد کن.");
            }
            headerHost = IDN.toASCII(candidateHeader, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
            if (headerHost.length() > 253 || !headerHost.contains(".")) {
                throw new IllegalArgumentException("Host header معتبر نیست.");
            }
            if (rawPath.isEmpty() || !rawPath.startsWith("/") || rawPath.length() > 512 ||
                rawPath.contains(" ") || rawPath.indexOf(13) >= 0 || rawPath.indexOf(10) >= 0 || rawPath.contains("#")) {
                throw new IllegalArgumentException("مسیر WebSocket باید با / شروع شود و فاصله یا # نداشته باشد.");
            }
            wsPath = rawPath;
            ports = parsePorts(portsInput.getText().toString());
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage() == null ? "تنظیمات نامعتبر است" : ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        int count = Integer.parseInt(String.valueOf(countSpinner.getSelectedItem()));
        lastHost = host;
        stopScan = false;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        copyButton.setEnabled(false);
        lastCopy = "";
        progressBar.setProgress(0);
        resultsText.setText("در حال آماده‌سازی نمونه‌های IP…");
        statusText.setText("در حال دریافت یا بازیابی آخرین فهرست IPهای Cloudflare…");
        progressText.setText("شروع اسکن");
        scanExecutor = null;
        Thread setupThread = new Thread(() -> runScan(host, headerHost, wsPath, ports, count), "hadi-netcheck-setup");
        setupThread.setDaemon(true);
        setupThread.start();
    }

    private List<Integer> parsePorts(String source) {
        Set<Integer> accepted = new HashSet<>(Arrays.asList(443, 2053, 2083, 2087, 2096, 8443));
        Set<Integer> selected = new java.util.LinkedHashSet<>();
        for (String part : source.split(",")) {
            if (part.trim().isEmpty()) continue;
            int port = Integer.parseInt(part.trim());
            if (!accepted.contains(port)) throw new IllegalArgumentException("فقط پورت‌های TLS پشتیبانی‌شده Cloudflare را وارد کن.");
            selected.add(port);
        }
        if (selected.isEmpty()) throw new IllegalArgumentException("حداقل یک پورت وارد کن.");
        return new ArrayList<>(selected);
    }

    private void runScan(String host, String headerHost, String wsPath, List<Integer> ports, int count) {
        List<String> cidrs = fetchCloudflareCidrs();
        final String rangeSource;
        if (!cidrs.isEmpty()) {
            saveCachedCidrs(cidrs);
            rangeSource = "فهرست تازه از Cloudflare";
        } else {
            cidrs = loadCachedCidrs();
            if (!cidrs.isEmpty()) {
                rangeSource = "فهرست ذخیره‌شده؛ حالت آفلاین";
            } else {
                cidrs = fallbackCidrs();
                rangeSource = "فهرست پشتیبان داخلی؛ ممکن است قدیمی باشد";
            }
        }
        List<Cidr> ranges = new ArrayList<>();
        for (String item : cidrs) {
            try { ranges.add(new Cidr(item.trim())); } catch (Exception ignored) {}
        }
        if (ranges.isEmpty()) {
            finishUi("لیست بازه‌های IP در دسترس نیست؛ اتصال اینترنت را بررسی کن.", "", 0);
            return;
        }
        List<String> ips = sampleIps(ranges, count);
        List<ProbeResult> successes = Collections.synchronizedList(new ArrayList<ProbeResult>());
        final int total = ips.size() * ports.size();

        ExecutorService pool = Executors.newFixedThreadPool(24);
        scanExecutor = pool;
        CompletionService<ProbeResult> queue = new ExecutorCompletionService<>(pool);
        List<Future<ProbeResult>> futures = new ArrayList<>();
        for (String ip : ips) {
            for (int port : ports) {
                final String fip = ip;
                final int fp = port;
                futures.add(queue.submit(() -> probe(fip, fp, host, headerHost, wsPath)));
            }
        }

        int finished = 0;
        try {
            while (finished < total && !stopScan) {
                Future<ProbeResult> completed = queue.poll(350, TimeUnit.MILLISECONDS);
                if (completed == null) continue;
                try {
                    ProbeResult r = completed.get();
                    if (r.ok) successes.add(r);
                } catch (Exception ignored) {}
                finished++;
                final int done = finished;
                runOnUiThread(() -> {
                    progressBar.setProgress((int)(done * 100.0 / Math.max(1, total)));
                    progressText.setText(done + " / " + total + " اتصال آزمایش شد");
                    statusText.setText("در حال تست TCP + TLS روی شبکه فعلی گوشی…");
                });
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            if (stopScan) {
                for (Future<?> f : futures) f.cancel(true);
                pool.shutdownNow();
            } else {
                pool.shutdown();
            }
        }

        if (stopScan) {
            finishUi("اسکن توسط کاربر متوقف شد. نتایج موفق تا اینجا نگه داشته شده‌اند.", "", successes.size());
            return;
        }
        if (successes.isEmpty()) {
            finishUi("هیچ IP با اتصال TLS معتبر برای این SNI پیدا نشد. ممکن است این شبکه یا دامنه انتخاب‌شده مناسب نباشد.", "", 0);
            return;
        }

        Collections.sort(successes, Comparator.comparingLong(r -> r.ms));
        List<ProbeResult> shortlist = new ArrayList<>(successes.subList(0, Math.min(20, successes.size())));
        runOnUiThread(() -> {
            statusText.setText("مرحله دوم: بازآزمایی " + shortlist.size() + " نامزد برتر برای پایداری…");
            progressBar.setProgress(100);
            progressText.setText("اسکن اولیه تمام شد؛ در حال تأیید دوباره");
        });

        ExecutorService verifyPool = Executors.newFixedThreadPool(Math.min(8, shortlist.size()));
        CompletionService<ProbeResult> verification = new ExecutorCompletionService<>(verifyPool);
        List<Future<ProbeResult>> verificationFutures = new ArrayList<>();
        for (ProbeResult initial : shortlist) {
            verificationFutures.add(verification.submit(() -> verify(initial.ip, initial.port, host, headerHost, wsPath)));
        }
        List<ProbeResult> verified = new ArrayList<>();
        try {
            int completedVerifications = 0;
            while (completedVerifications < verificationFutures.size() && !stopScan) {
                Future<ProbeResult> f = verification.poll(400, TimeUnit.MILLISECONDS);
                if (f == null) continue;
                completedVerifications++;
                try {
                    ProbeResult r = f.get();
                    if (r.ok) verified.add(r);
                } catch (Exception ignored) {}
                final int done = completedVerifications;
                runOnUiThread(() -> progressText.setText("تأیید پایداری: " + done + " / " + verificationFutures.size()));
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            if (stopScan) {
                for (Future<?> f : verificationFutures) f.cancel(true);
                verifyPool.shutdownNow();
            } else verifyPool.shutdown();
        }

        if (verified.isEmpty()) {
            finishUi("هیچ IP در بازآزمایی کامل WebSocket موفق نشد. SNI، Host و مسیر را دقیقاً با کانفیگت یکی کن.", "", 0);
            return;
        }
        Collections.sort(verified, (a,b) -> {
            if (a.successes != b.successes) return Integer.compare(b.successes, a.successes);
            return Long.compare(a.ms, b.ms);
        });
        StringBuilder output = new StringBuilder();
        StringBuilder copy = new StringBuilder();
        int shown = Math.min(30, verified.size());
        for (int i = 0; i < shown; i++) {
            ProbeResult r = verified.get(i);
            output.append(String.format(Locale.US, "%2d. %s:%d   %d ms   %d/3 WS\n", i + 1, r.ip, r.port, r.ms, r.successes));
            copy.append("IP: ").append(r.ip).append("\n")
                .append("Port: ").append(r.port).append("\n")
                .append("TLS SNI: ").append(host).append("\n")
                .append("Host: ").append(headerHost).append("\n")
                .append("Transport: WebSocket + TLS\n")
                .append("Path: ").append(wsPath).append("\n")
                .append("Successful WS handshakes: ").append(r.successes).append("/3\n\n");
        }
        final String finalOutput = output.toString();
        final String finalCopy = copy.toString().trim();
        final int found = successes.size();
        final int verifiedCount = verified.size();
        runOnUiThread(() -> {
            resultsText.setText(finalOutput);
            lastCopy = finalCopy;
            copyButton.setEnabled(!lastCopy.isEmpty());
            statusText.setText("اسکن تمام شد؛ " + verifiedCount + " IP با WebSocket واقعی تأیید شد (از " + found + " TLS اولیه).");
            progressBar.setProgress(100);
            progressText.setText("منبع: " + rangeSource + " · نمونه: " + ips.size() + " · پورت‌ها: " + ports + " · SNI: " + host + " · Host: " + headerHost + " · Path: " + wsPath);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    private void cancelScan() {
        stopScan = true;
        ExecutorService e = scanExecutor;
        if (e != null) e.shutdownNow();
        stopButton.setEnabled(false);
        statusText.setText("در حال توقف امن اسکن…");
    }

    private List<ProbeResult> unusedResults() { return new ArrayList<>(); }

    private ProbeResult verify(String ip, int port, String host, String headerHost, String wsPath) {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 3 && !stopScan; i++) {
            ProbeResult r = probe(ip, port, host, headerHost, wsPath);
            if (r.ok) times.add(r.ms);
        }
        if (times.isEmpty()) return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
        Collections.sort(times);
        long median = times.get(times.size() / 2);
        return new ProbeResult(ip, port, true, median, times.size());
    }

    private ProbeResult probe(String ip, int port, String host, String headerHost, String wsPath) {
        if (stopScan || Thread.currentThread().isInterrupted()) return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
        long start = System.nanoTime();
        Socket raw = new Socket();
        SSLSocket tls = null;
        try {
            raw.connect(new InetSocketAddress(ip, port), 1300);
            raw.setSoTimeout(2200);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            tls = (SSLSocket) factory.createSocket(raw, host, port, true);
            SSLParameters params = tls.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setServerNames(Collections.singletonList(new SNIHostName(host)));
            tls.setSSLParameters(params);
            tls.setSoTimeout(2200);
            tls.startHandshake();

            byte[] random = new byte[16];
            new SecureRandom().nextBytes(random);
            String wsKey = Base64.getEncoder().encodeToString(random);
            String authority = headerHost + (port == 443 ? "" : ":" + port);
            String request = "GET " + wsPath + " HTTP/1.1\\r\\n"
                + "Host: " + authority + "\\r\\n"
                + "Upgrade: websocket\\r\\n"
                + "Connection: Upgrade\\r\\n"
                + "Sec-WebSocket-Key: " + wsKey + "\\r\\n"
                + "Sec-WebSocket-Version: 13\\r\\n"
                + "User-Agent: HadiNetCheck/1.1\\r\\n"
                + "\\r\\n";
            OutputStream out = tls.getOutputStream();
            out.write(request.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            BufferedInputStream in = new BufferedInputStream(tls.getInputStream());
            String statusLine = readHttpLine(in, 2048);
            if (statusLine == null || !statusLine.matches("HTTP/1\\\\.[01] 101(?: .*)?")) {
                return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
            }
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            int headerBytes = statusLine.length();
            while (headerBytes < 16384) {
                String line = readHttpLine(in, 4096);
                if (line == null) return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
                headerBytes += line.length() + 2;
                if (line.isEmpty()) break;
                int colon = line.indexOf(':');
                if (colon > 0) headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT), line.substring(colon + 1).trim());
            }
            String upgrade = headers.getOrDefault("upgrade", "");
            String connection = headers.getOrDefault("connection", "");
            String accept = headers.getOrDefault("sec-websocket-accept", "");
            String expectedAccept = websocketAccept(wsKey);
            if (!"websocket".equalsIgnoreCase(upgrade)
                || !connection.toLowerCase(Locale.ROOT).contains("upgrade")
                || !expectedAccept.equals(accept)) {
                return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
            }
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new ProbeResult(ip, port, true, elapsed, 1);
        } catch (Exception ignored) {
            return new ProbeResult(ip, port, false, Long.MAX_VALUE, 0);
        } finally {
            try { if (tls != null) tls.close(); } catch (Exception ignored) {}
            try { raw.close(); } catch (Exception ignored) {}
        }
    }

    private String readHttpLine(BufferedInputStream in, int max) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int b;
        while (buffer.size() <= max && (b = in.read()) != -1) {
            if (b == '\\n') {
                byte[] bytes = buffer.toByteArray();
                int len = bytes.length;
                if (len > 0 && bytes[len - 1] == '\\r') len--;
                return new String(bytes, 0, len, StandardCharsets.ISO_8859_1);
            }
            buffer.write(b);
        }
        return null;
    }

    private String websocketAccept(String key) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.ISO_8859_1));
        return Base64.getEncoder().encodeToString(digest);
    }

    private List<String> fetchCloudflareCidrs() {
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) new URL("https://www.cloudflare.com/ips-v4").openConnection();
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            StringBuilder body = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null && body.length() < 10000) {
                    String trimmed = line.trim();
                    if (trimmed.matches("\\d{1,3}(?:\\.\\d{1,3}){3}/\\d{1,2}")) body.append(trimmed).append("\n");
                }
            }
            List<String> result = new ArrayList<>();
            for (String line : body.toString().split("\n")) if (!line.trim().isEmpty()) result.add(line.trim());
            return result;
        } catch (Exception ignored) {
            return new ArrayList<>();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private List<String> fallbackCidrs() {
        return Arrays.asList(
            "173.245.48.0/20", "103.21.244.0/22", "103.22.200.0/22",
            "103.31.4.0/22", "141.101.64.0/18", "108.162.192.0/18",
            "190.93.240.0/20", "188.114.96.0/20", "197.234.240.0/22",
            "198.41.128.0/17", "162.158.0.0/15", "104.16.0.0/13",
            "104.24.0.0/14", "172.64.0.0/13", "131.0.72.0/22"
        );
    }

    private List<String> sampleIps(List<Cidr> ranges, int count) {
        List<String> output = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        long total = 0;
        for (Cidr c : ranges) total += Math.max(1, c.size - 2);
        Random random = new Random();
        int attempts = 0;
        while (output.size() < count && attempts < count * 20) {
            attempts++;
            double pick = random.nextDouble() * total;
            Cidr chosen = ranges.get(ranges.size() - 1);
            for (Cidr c : ranges) {
                pick -= Math.max(1, c.size - 2);
                if (pick < 0) { chosen = c; break; }
            }
            String ip = chosen.randomHost(random);
            if (seen.add(ip)) output.add(ip);
        }
        return output;
    }

    private void finishUi(String message, String output, int found) {
        runOnUiThread(() -> {
            statusText.setText(message);
            resultsText.setText(output.isEmpty() ? message : output);
            progressBar.setProgress(100);
            progressText.setText("نتیجه: " + found + " اتصال موفق");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            copyButton.setEnabled(!lastCopy.isEmpty());
        });
    }

    private void copyResults() {
        if (lastCopy == null || lastCopy.isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Hadi NetCheck results", lastCopy));
        Toast.makeText(this, "IPهای پیدا‌شده کپی شدند", Toast.LENGTH_SHORT).show();
    }

    private static final class ProbeResult {
        final String ip;
        final int port;
        final boolean ok;
        final long ms;
        final int successes;
        ProbeResult(String ip, int port, boolean ok, long ms, int successes) {
            this.ip = ip; this.port = port; this.ok = ok; this.ms = ms; this.successes = successes;
        }
    }

    private static final class Cidr {
        final long network;
        final int prefix;
        final long size;
        Cidr(String text) {
            String[] parts = text.split("/");
            if (parts.length != 2) throw new IllegalArgumentException("bad CIDR");
            network = ipToLong(parts[0]);
            prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 30) throw new IllegalArgumentException("bad prefix");
            size = 1L << (32 - prefix);
        }
        String randomHost(Random random) {
            long offset = 1 + (long)(random.nextDouble() * Math.max(1, size - 2));
            return longToIp((network + offset) & 0xffffffffL);
        }
        private static long ipToLong(String ip) {
            String[] octets = ip.split("\\.");
            if (octets.length != 4) throw new IllegalArgumentException("bad IP");
            long result = 0;
            for (String octet : octets) {
                int n = Integer.parseInt(octet);
                if (n < 0 || n > 255) throw new IllegalArgumentException("bad octet");
                result = (result << 8) | n;
            }
            return result & 0xffffffffL;
        }
        private static String longToIp(long ip) {
            return ((ip >> 24) & 255) + "." + ((ip >> 16) & 255) + "." + ((ip >> 8) & 255) + "." + (ip & 255);
        }
    }
}
