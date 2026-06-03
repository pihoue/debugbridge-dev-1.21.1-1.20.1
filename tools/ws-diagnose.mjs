const url = 'ws://127.0.0.1:9876';
console.log(`Connecting to ${url}...`);
const ws = new WebSocket(url);
const t = setTimeout(() => {
    console.log('TIMEOUT after 5s — no connection established');
    console.log('Check: Is the game running? Is port 9876 listening?');
    process.exit(1);
}, 5000);
ws.addEventListener('open', () => {
    clearTimeout(t);
    console.log('CONNECTED');
    ws.send(JSON.stringify({id:'1',type:'status',payload:{}}));
});
ws.addEventListener('message', (e) => {
    console.log('RECEIVED:', e.data);
    ws.close();
});
ws.addEventListener('error', (e) => {
    console.log('WEBSOCKET ERROR');
    console.log('  type:', e.type);
    console.log('  message:', e.message);
    console.log('  error:', e.error);
    console.log('  target.readyState:', ws.readyState);
    console.log('  target.url:', ws.url);
});
ws.addEventListener('close', (e) => {
    console.log('CLOSED: code=' + e.code + ' reason=' + e.reason + ' wasClean=' + e.wasClean);
});
