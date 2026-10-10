const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 8080;
const SUPABASE_HOST = 'wjtyegbvqubxtujruifo.supabase.co';
const SUPABASE_KEY = process.env.SUPABASE_KEY || 'YOUR_SUPABASE_ANON_KEY_HERE';

const server = http.createServer((req, res) => {
    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Prefer');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const parsedUrl = url.parse(req.url);

    // Proxy /api/rest/v1/* to Supabase PostgREST with Server User-Agent (avoids browser secret-key block)
    if (parsedUrl.pathname && parsedUrl.pathname.startsWith('/api/rest/v1/')) {
        const targetPath = parsedUrl.path.replace('/api/rest/v1/', '/rest/v1/');
        let bodyChunks = [];
        req.on('data', chunk => bodyChunks.push(chunk));
        req.on('end', () => {
            const bodyBuffer = Buffer.concat(bodyChunks);
            const options = {
                hostname: SUPABASE_HOST,
                port: 443,
                path: targetPath,
                method: req.method,
                headers: {
                    'apikey': SUPABASE_KEY,
                    'Authorization': `Bearer ${SUPABASE_KEY}`,
                    'User-Agent': 'SafeNova-Responder-Command/2.0',
                    'Content-Type': 'application/json',
                    'Prefer': req.headers['prefer'] || 'return=representation'
                }
            };

            const proxyReq = https.request(options, proxyRes => {
                res.writeHead(proxyRes.statusCode || 200, {
                    'Content-Type': proxyRes.headers['content-type'] || 'application/json',
                    'Access-Control-Allow-Origin': '*'
                });
                proxyRes.pipe(res);
            });

            proxyReq.on('error', err => {
                res.writeHead(502, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: err.message }));
            });

            if (bodyBuffer.length > 0) {
                proxyReq.write(bodyBuffer);
            }
            proxyReq.end();
        });
        return;
    }

    // Serve static files (index.html)
    let filePath = path.join(__dirname, parsedUrl.pathname === '/' ? 'index.html' : parsedUrl.pathname);
    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes = {
        '.html': 'text/html; charset=utf-8',
        '.js': 'text/javascript; charset=utf-8',
        '.css': 'text/css; charset=utf-8',
        '.json': 'application/json'
    };

    fs.readFile(filePath, (err, content) => {
        if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('404 Not Found');
            return;
        }
        res.writeHead(200, { 'Content-Type': mimeTypes[ext] || 'text/plain' });
        res.end(content);
    });
});

server.listen(PORT, () => {
    console.log(`============================================================`);
    console.log(`🛡️  SAFENOVA Responder Dashboard Live at http://localhost:${PORT}`);
    console.log(`🔗 Connected to Supabase: https://${SUPABASE_HOST}`);
    console.log(`============================================================`);
});
