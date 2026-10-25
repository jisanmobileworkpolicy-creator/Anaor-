const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.PORT || process.env.SERVER_PORT || 3000;
const ADMIN_SECRET_KEY = process.env.ADMIN_SECRET_KEY || 'jisan786';
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL ? process.env.PUBLIC_BASE_URL.replace(/\/+$/, '') : null;

const APK_FILENAME = 'JISAN_MOBILE.apk';
const APK_PATH = path.join(__dirname, APK_FILENAME);

const DATA_DIR = path.join(__dirname, 'data');
const APK_PERSISTENT_PATH = path.join(DATA_DIR, APK_FILENAME);
const UPLOADS_DIR = path.join(__dirname, 'uploads');
const PRODUCTS_FILE = path.join(DATA_DIR, 'products.json');
const ORDERS_FILE = path.join(DATA_DIR, 'orders.json');
const APP_UPDATE_FILE = path.join(DATA_DIR, 'app_update.json');

function getValidApkPath() {
  if (fs.existsSync(APK_PERSISTENT_PATH)) return APK_PERSISTENT_PATH;
  if (fs.existsSync(APK_PATH)) return APK_PATH;
  const buildApk = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');
  if (fs.existsSync(buildApk)) return buildApk;
  return null;
}

// Ensure directories exist
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}
if (!fs.existsSync(UPLOADS_DIR)) {
  fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

// Initial products (clean for production)
const INITIAL_PRODUCTS = [];

// Initialize files if not existing
if (!fs.existsSync(PRODUCTS_FILE)) {
  fs.writeFileSync(PRODUCTS_FILE, JSON.stringify(INITIAL_PRODUCTS, null, 2), 'utf8');
}
if (!fs.existsSync(ORDERS_FILE)) {
  fs.writeFileSync(ORDERS_FILE, JSON.stringify([], null, 2), 'utf8');
}

function readProducts() {
  try {
    const data = fs.readFileSync(PRODUCTS_FILE, 'utf8');
    return JSON.parse(data);
  } catch (err) {
    console.error('Error reading products:', err);
    return INITIAL_PRODUCTS;
  }
}

function writeProducts(products) {
  try {
    fs.writeFileSync(PRODUCTS_FILE, JSON.stringify(products, null, 2), 'utf8');
    return true;
  } catch (err) {
    console.error('Error writing products:', err);
    return false;
  }
}

function readOrders() {
  try {
    const data = fs.readFileSync(ORDERS_FILE, 'utf8');
    return JSON.parse(data);
  } catch (err) {
    return [];
  }
}

function writeOrders(orders) {
  try {
    fs.writeFileSync(ORDERS_FILE, JSON.stringify(orders, null, 2), 'utf8');
    return true;
  } catch (err) {
    return false;
  }
}

function readAppUpdate(baseUrl = '') {
  const defaultDownloadUrl = baseUrl ? `${baseUrl}/JISAN_MOBILE.apk` : '/JISAN_MOBILE.apk';
  try {
    if (fs.existsSync(APP_UPDATE_FILE)) {
      const data = fs.readFileSync(APP_UPDATE_FILE, 'utf8');
      const parsed = JSON.parse(data);
      if (!parsed.downloadUrl || parsed.downloadUrl.trim() === '') {
        parsed.downloadUrl = defaultDownloadUrl;
      } else if (parsed.downloadUrl.startsWith('/') && baseUrl) {
        parsed.downloadUrl = `${baseUrl}${parsed.downloadUrl}`;
      }
      return parsed;
    }
  } catch (err) {
    console.error('Error reading app_update.json:', err);
  }
  return {
    versionCode: 1,
    versionName: '1.0',
    releaseNotes: 'JISAN MOBILE অফিসিয়াল অ্যাপ।',
    downloadUrl: defaultDownloadUrl,
    forceUpdate: false,
    releaseDate: '২৩ অক্টো, ২০২৬',
    minSupportedVersion: 1
  };
}

function writeAppUpdate(updateData) {
  try {
    fs.writeFileSync(APP_UPDATE_FILE, JSON.stringify(updateData, null, 2), 'utf8');
    return true;
  } catch (err) {
    console.error('Error writing app_update.json:', err);
    return false;
  }
}

// Request parser helper
function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 25 * 1024 * 1024) { // 25MB max
        reject(new Error('Payload too large'));
      }
    });
    req.on('end', () => {
      resolve(body);
    });
    req.on('error', reject);
  });
}

function isAuthorizedAdmin(req) {
  const authHeader = req.headers['authorization'] || '';
  const adminKeyHeader = req.headers['x-admin-key'] || '';
  return authHeader === `Bearer ${ADMIN_SECRET_KEY}` || adminKeyHeader === ADMIN_SECRET_KEY;
}

const server = http.createServer(async (req, res) => {
  let baseUrl = PUBLIC_BASE_URL;
  if (!baseUrl) {
    const host = req.headers.host || 'localhost:3000';
    let protocol = req.headers['x-forwarded-proto'] || 'https';
    if (!host.startsWith('localhost') && !host.startsWith('127.0.0.1')) {
      protocol = 'https';
    }
    baseUrl = `${protocol}://${host}`;
  }
  const url = new URL(req.url, baseUrl);
  const pathname = url.pathname;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Admin-Key, X-Requested-With');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  console.log(`[${new Date().toISOString()}] ${req.method} ${pathname}`);

  // 1. Direct APK download routes
  if (
    pathname === '/JISAN_MOBILE.apk' ||
    pathname === '/download' ||
    pathname === '/app-debug.apk' ||
    pathname.endsWith('.apk') ||
    url.searchParams.get('download') === 'direct'
  ) {
    const validApk = getValidApkPath();
    if (!validApk || !fs.existsSync(validApk)) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('APK ফাইল পাওয়া যায়নি।');
      return;
    }

    const stat = fs.statSync(validApk);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': `attachment; filename="${APK_FILENAME}"`,
      'Content-Length': stat.size,
      'Cache-Control': 'no-cache',
    });

    const readStream = fs.createReadStream(validApk);
    readStream.pipe(res);
    return;
  }

  // 2. Uploaded image static serving (/uploads/:filename)
  if (pathname.startsWith('/uploads/')) {
    const filename = path.basename(pathname);
    const filePath = path.join(UPLOADS_DIR, filename);

    if (fs.existsSync(filePath)) {
      const ext = path.extname(filename).toLowerCase();
      let contentType = 'image/jpeg';
      if (ext === '.png') contentType = 'image/png';
      else if (ext === '.webp') contentType = 'image/webp';
      else if (ext === '.gif') contentType = 'image/gif';

      const stat = fs.statSync(filePath);
      res.writeHead(200, {
        'Content-Type': contentType,
        'Content-Length': stat.size,
        'Cache-Control': 'public, max-age=86400',
      });
      fs.createReadStream(filePath).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Image not found' }));
      return;
    }
  }

  // 3. REST API: GET /api/health
  if (pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'ok',
      version: '1.1',
      productsCount: readProducts().length,
      ordersCount: readOrders().length,
      timestamp: Date.now()
    }));
    return;
  }

  // 4. REST API: /api/products
  if (pathname === '/api/products') {
    if (req.method === 'GET') {
      const products = readProducts();
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: true, count: products.length, products }));
      return;
    }

    if (req.method === 'POST') {
      if (!isAuthorizedAdmin(req)) {
        res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ success: false, error: 'Unauthorized: Admin authorization required' }));
        return;
      }
      try {
        const rawBody = await parseBody(req);
        const data = JSON.parse(rawBody);
        const products = readProducts();

        if (Array.isArray(data)) {
          // Bulk update / sync
          const map = new Map(products.map(p => [p.id, p]));
          for (const item of data) {
            if (item.id) map.set(item.id, item);
          }
          const updatedList = Array.from(map.values());
          writeProducts(updatedList);
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, count: updatedList.length }));
          return;
        } else if (data && data.id) {
          // Single product upsert
          const index = products.findIndex(p => p.id === data.id);
          if (index >= 0) {
            products[index] = data;
          } else {
            products.unshift(data);
          }
          writeProducts(products);
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, product: data }));
          return;
        } else {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Invalid product data: missing id' }));
          return;
        }
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
        return;
      }
    }
  }

  // 5. REST API: DELETE /api/products/:id
  if (pathname.startsWith('/api/products/')) {
    const id = decodeURIComponent(pathname.substring('/api/products/'.length));
    if (req.method === 'DELETE') {
      if (!isAuthorizedAdmin(req)) {
        res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ success: false, error: 'Unauthorized: Admin authorization required' }));
        return;
      }
      const products = readProducts();
      const filtered = products.filter(p => p.id !== id);
      writeProducts(filtered);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, deletedId: id, remaining: filtered.length }));
      return;
    }
  }

  // 6. REST API: POST /api/upload (Base64 image upload)
  if (pathname === '/api/upload' && req.method === 'POST') {
    if (!isAuthorizedAdmin(req)) {
      res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: false, error: 'Unauthorized: Admin authorization required' }));
      return;
    }
    try {
      const rawBody = await parseBody(req);
      const data = JSON.parse(rawBody);

      if (!data || !data.data) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Missing base64 data' }));
        return;
      }

      // Handle base64 string (strip data:image/xxx;base64, if present)
      let base64Str = data.data;
      if (base64Str.includes(';base64,')) {
        base64Str = base64Str.split(';base64,')[1];
      }

      const buffer = Buffer.from(base64Str, 'base64');
      const ext = data.fileName ? path.extname(data.fileName).toLowerCase() : '.jpg';
      const safeExt = (ext === '.png' || ext === '.webp' || ext === '.jpeg') ? ext : '.jpg';
      const filename = `img_${Date.now()}_${crypto.randomBytes(4).toString('hex')}${safeExt}`;
      const destPath = path.join(UPLOADS_DIR, filename);

      fs.writeFileSync(destPath, buffer);

      // Return public URL based on the actual request host
      const publicUrl = `${baseUrl}/uploads/${filename}`;
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        success: true,
        filename,
        url: publicUrl
      }));
      return;
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
      return;
    }
  }

  // 7. REST API: /api/orders
  if (pathname === '/api/orders') {
    if (req.method === 'GET') {
      const orders = readOrders();
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: true, count: orders.length, orders }));
      return;
    }

    if (req.method === 'POST') {
      try {
        const rawBody = await parseBody(req);
        const order = JSON.parse(rawBody);
        const orders = readOrders();
        orders.unshift(order);
        writeOrders(orders);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, orderNumber: order.orderNumber }));
        return;
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
        return;
      }
    }
  }

  // 8. REST API: PUT /api/orders/:orderNumber/status
  if (pathname.startsWith('/api/orders/') && req.method === 'PUT') {
    if (!isAuthorizedAdmin(req)) {
      res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: false, error: 'Unauthorized: Admin authorization required' }));
      return;
    }
    const parts = pathname.split('/');
    const orderNumber = parts[3];
    try {
      const rawBody = await parseBody(req);
      const { status, notes } = JSON.parse(rawBody);
      const orders = readOrders();
      const order = orders.find(o => o.orderNumber === orderNumber);
      if (order) {
        order.status = status;
        if (notes) order.trackingNotes = notes;
        order.updatedAt = Date.now();
        writeOrders(orders);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, order }));
      } else {
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Order not found' }));
      }
      return;
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
      return;
    }
  }

  // 9. App Update Check API: GET /api/app/check-update
  if (pathname === '/api/app/check-update' || pathname === '/api/check-update') {
    const updateInfo = readAppUpdate(baseUrl);
    res.writeHead(200, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
    });
    res.end(JSON.stringify({
      success: true,
      ...updateInfo
    }));
    return;
  }

  // 10a. APK Upload API: POST /api/admin/app/upload-apk
  if (pathname === '/api/admin/app/upload-apk' && req.method === 'POST') {
    if (!isAuthorizedAdmin(req)) {
      res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: false, error: 'Unauthorized: অ্যাডমিন প্রমাণীকরণ প্রয়োজন' }));
      return;
    }

    const writeStream = fs.createWriteStream(APK_PERSISTENT_PATH);
    let bytesReceived = 0;

    req.on('data', chunk => {
      bytesReceived += chunk.length;
      writeStream.write(chunk);
    });

    req.on('end', () => {
      writeStream.end(() => {
        try {
          fs.copyFileSync(APK_PERSISTENT_PATH, APK_PATH);
        } catch (copyErr) {
          console.warn('Could not copy to root APK_PATH:', copyErr.message);
        }

        const publicUrl = `${baseUrl}/JISAN_MOBILE.apk`;

        const currentUpdate = readAppUpdate(baseUrl);
        currentUpdate.downloadUrl = publicUrl;
        currentUpdate.updatedAt = Date.now();
        writeAppUpdate(currentUpdate);

        res.writeHead(200, {
          'Content-Type': 'application/json; charset=utf-8',
          'Cache-Control': 'no-cache'
        });
        res.end(JSON.stringify({
          success: true,
          message: 'আসল APK ফাইল সফলভাবে persistent storage-এ আপলোড ও সংরক্ষিত হয়েছে!',
          downloadUrl: publicUrl,
          filename: APK_FILENAME,
          fileSize: bytesReceived
        }));
      });
    });

    req.on('error', err => {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: false, error: err.message }));
    });
    return;
  }

  // 10b. Sync Current Build APK API: POST /api/admin/app/sync-current-apk
  if (pathname === '/api/admin/app/sync-current-apk' && req.method === 'POST') {
    if (!isAuthorizedAdmin(req)) {
      res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: false, error: 'Unauthorized: অ্যাডমিন প্রমাণীকরণ প্রয়োজন' }));
      return;
    }

    const validApk = getValidApkPath();
    if (!validApk || !fs.existsSync(validApk)) {
      res.writeHead(404, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ success: false, error: 'সার্ভারে বর্তমান বিল্ডের APK ফাইল খুঁজে পাওয়া যায়নি।' }));
      return;
    }

    if (validApk !== APK_PERSISTENT_PATH) {
      fs.copyFileSync(validApk, APK_PERSISTENT_PATH);
    }
    if (validApk !== APK_PATH) {
      fs.copyFileSync(validApk, APK_PATH);
    }

    const stat = fs.statSync(APK_PERSISTENT_PATH);
    const publicUrl = `${baseUrl}/JISAN_MOBILE.apk`;

    const currentUpdate = readAppUpdate(baseUrl);
    currentUpdate.downloadUrl = publicUrl;
    currentUpdate.updatedAt = Date.now();
    writeAppUpdate(currentUpdate);

    res.writeHead(200, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    res.end(JSON.stringify({
      success: true,
      message: 'বর্তমান বিল্ডের APK সফলভাবে পারসিস্টেন্ট স্টোরেজে সংরক্ষিত হয়েছে!',
      downloadUrl: publicUrl,
      filename: APK_FILENAME,
      fileSize: stat.size
    }));
    return;
  }

  // 10c. App Update Publish API: POST /api/admin/app/publish-update
  if ((pathname === '/api/admin/app/publish-update' || pathname === '/api/app/publish-update') && req.method === 'POST') {
    if (!isAuthorizedAdmin(req)) {
      res.writeHead(401, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({
        success: false,
        error: 'Unauthorized: অ্যাডমিন প্রমাণীকরণ প্রয়োজন (Valid admin authorization required)'
      }));
      return;
    }

    try {
      const rawBody = await parseBody(req);
      const data = JSON.parse(rawBody);
      const current = readAppUpdate(baseUrl);

      let targetDownloadUrl = (data.downloadUrl !== undefined ? data.downloadUrl : (current.downloadUrl || "")).trim();

      // Requirement 6: Block publish if URL is empty or invalid
      if (!targetDownloadUrl || targetDownloadUrl === '') {
        const validApk = getValidApkPath();
        if (validApk) {
          targetDownloadUrl = `${baseUrl}/JISAN_MOBILE.apk`;
        } else {
          res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
          res.end(JSON.stringify({
            success: false,
            error: 'APK Download URL ফাঁকা রাখা যাবে না! অনুগ্রহ করে প্রথমে বর্তমান বিল্ডের APK আপলোড করুন অথবা একটি বৈধ HTTPS ডাউনলোড লিঙ্ক দিন।'
          }));
          return;
        }
      }

      if (targetDownloadUrl.startsWith('/')) {
        targetDownloadUrl = `${baseUrl}${targetDownloadUrl}`;
      } else if (!targetDownloadUrl.startsWith('http://') && !targetDownloadUrl.startsWith('https://')) {
        res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({
          success: false,
          error: 'অবৈধ APK ডাউনলোড লিংক! লিংকটি অবশ্যই https:// বা http:// দিয়ে শুরু হতে হবে।'
        }));
        return;
      }

      const updated = {
        versionCode: parseInt(data.versionCode, 10) || current.versionCode || 1,
        versionName: (data.versionName || current.versionName || "1.0").trim(),
        releaseNotes: (data.releaseNotes || current.releaseNotes || "").trim(),
        downloadUrl: targetDownloadUrl,
        forceUpdate: Boolean(data.forceUpdate),
        releaseDate: data.releaseDate || new Date().toLocaleDateString('bn-BD', { day: 'numeric', month: 'short', year: 'numeric' }),
        minSupportedVersion: parseInt(data.minSupportedVersion, 10) || current.minSupportedVersion || 1,
        updatedAt: Date.now()
      };

      writeAppUpdate(updated);
      res.writeHead(200, {
        'Content-Type': 'application/json; charset=utf-8',
        'Cache-Control': 'no-cache'
      });
      res.end(JSON.stringify({
        success: true,
        message: 'অ্যাপ আপডেট সফলভাবে প্রকাশ করা হয়েছে!',
        updateInfo: updated
      }));
      return;
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
      return;
    }
  }

  // 11. Home / Landing page with instant auto-download and download button
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache',
  });

  const html = `<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JISAN MOBILE (জিসান মোবাইল) - অফিসিয়াল অ্যাপ ডাউনলোড ও লাইভ শপ</title>
  <meta http-equiv="refresh" content="1; url=/JISAN_MOBILE.apk">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hind Siliguri", sans-serif;
      background: linear-gradient(135deg, #0d1117 0%, #161b22 100%);
      color: #f0f6fc;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px;
      text-align: center;
    }
    .card {
      background: rgba(22, 27, 34, 0.95);
      border: 1px solid #30363d;
      border-radius: 20px;
      padding: 36px 28px;
      max-width: 440px;
      width: 100%;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
    }
    .logo {
      width: 80px;
      height: 80px;
      background: linear-gradient(135deg, #8a2be2, #4a00e0);
      color: #fff;
      font-size: 32px;
      font-weight: 800;
      border-radius: 22px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      box-shadow: 0 6px 20px rgba(138, 43, 226, 0.4);
      letter-spacing: 1px;
    }
    h1 {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
      color: #ffffff;
    }
    .badge {
      display: inline-block;
      background: rgba(46, 160, 67, 0.15);
      color: #3fb950;
      border: 1px solid rgba(46, 160, 67, 0.4);
      font-size: 13px;
      padding: 4px 12px;
      border-radius: 20px;
      margin-bottom: 18px;
      font-weight: 600;
    }
    p.desc {
      font-size: 14px;
      color: #8b949e;
      line-height: 1.6;
      margin-bottom: 24px;
    }
    .btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background: linear-gradient(135deg, #238636, #2ea043);
      color: #ffffff;
      text-decoration: none;
      padding: 16px 24px;
      font-size: 17px;
      font-weight: 700;
      border-radius: 14px;
      box-shadow: 0 4px 18px rgba(46, 160, 67, 0.4);
      transition: transform 0.15s ease, background 0.2s ease;
      margin-bottom: 16px;
    }
    .btn:hover {
      transform: translateY(-2px);
      background: linear-gradient(135deg, #2ea043, #3fb950);
    }
    .auto-notice {
      font-size: 13px;
      color: #58a6ff;
      margin-bottom: 24px;
      font-weight: 600;
    }
    .guide {
      background: #0d1117;
      border-radius: 12px;
      padding: 18px;
      text-align: left;
      font-size: 13px;
      color: #c9d1d9;
      line-height: 1.6;
    }
    .guide h3 {
      font-size: 14px;
      color: #f0f6fc;
      margin-bottom: 10px;
    }
    .guide ol {
      padding-left: 20px;
    }
    .guide li {
      margin-bottom: 6px;
    }
    .whatsapp-contact {
      margin-top: 18px;
      padding-top: 14px;
      border-top: 1px dashed #30363d;
      font-size: 13px;
      color: #a5d6a7;
    }
    .footer {
      margin-top: 24px;
      font-size: 12px;
      color: #6e7681;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="logo">JM</div>
    <h1>JISAN MOBILE</h1>
    <div class="badge">অফিসিয়াল অ্যান্ড্রয়েড অ্যাপ (v1.1 - লাইভ সিঙ্ক)</div>
    <p class="desc">মোবাইল ফোন ও গ্যাজেট কিনুন সুলভ মূল্যে। অ্যাডমিন যা প্রোডাক্ট আপলোড করবেন তা সরাসরি সব কাস্টমারের ফোনে মুহূর্তেই সিঙ্ক হবে!</p>

    <a href="/JISAN_MOBILE.apk" class="btn" id="dlBtn" download="JISAN_MOBILE.apk">
      <span>📲</span>
      <span>APK সরাসরি ডাউনলোড (19 MB)</span>
    </a>

    <div class="auto-notice" id="notice">
      ⏳ ডাউনলোড শুরু হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন।
    </div>

    <div class="guide">
      <h3>📋 ইন্সটল করার নিয়ম:</h3>
      <ol>
        <li>উপরের <strong>"APK সরাসরি ডাউনলোড"</strong> বাটনে ট্যাপ করুন।</li>
        <li>ব্রাউজারে <em>"Download anyway"</em> অপশন আসলে কনফার্ম করুন।</li>
        <li>ডাউনলোড শেষ হলে ফাইলে ট্যাপ করে <strong>Install</strong> করুন।</li>
      </ol>
      <div class="whatsapp-contact">
        💬 কোনো সমস্যা হলে WhatsApp করুন: <strong>+91 9382302242</strong>
      </div>
    </div>
  </div>

  <div class="footer">
    © 2026 JISAN MOBILE • বারোমাসিয়া হাজির হাট, সাগরপাড়া, মুর্শিদাবাদ
  </div>

  <script>
    window.addEventListener('load', function() {
      setTimeout(function() {
        var link = document.createElement('a');
        link.href = '/JISAN_MOBILE.apk';
        link.download = 'JISAN_MOBILE.apk';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        var n = document.getElementById('notice');
        if (n) n.innerHTML = '✅ ডাউনলোড শুরু হয়েছে! স্বয়ংক্রিয়ভাবে না হলে উপরের বাটনে ক্লিক করুন।';
      }, 500);
    });
  </script>
</body>
</html>
`;

  res.end(html);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`JISAN MOBILE Web, API & APK server listening on 0.0.0.0:${PORT}`);
});
