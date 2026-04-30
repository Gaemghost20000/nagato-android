# Nagato Auto-Start Script (Post-Bootstrap)

## Location After Bootstrap Extracts
```
/data/data/com.nagato.agent/files/home/.nagato_setup.sh
```

## Content
```bash
#!/data/data/com.nagato.agent/files/usr/bin/bash
export PATH=/data/data/com.nagato.agent/files/usr/bin:$PATH
export PREFIX=/data/data/com.nagato.agent/files/usr

# Prevent double-run
if [ -f ~/.nagato_ready ]; then
    echo "Nagato already set up. Run 'hermes agent' and 'hermes webui' manually."
    exit 0
fi

echo "=== Nagato Setup ==="
echo "Installing dependencies..."

pkg update -y
pkg install -y python python-pip git curl openssl nodejs

# Install hermes-agent with webui support
echo "Installing hermes-agent..."
pip install --upgrade pip
pip install hermes-agent[web]

# Mark setup complete
touch ~/.nagato_ready

echo "=== Setup complete. Starting agent... ==="
nohup hermes agent > ~/.nagato_agent.log 2>&1 &
sleep 2
nohup hermes webui > ~/.nagato_webui.log 2>&1 &

echo "Nagato agent running on http://localhost:8787"
```

## When It Runs
Triggered by NagatoActivity.runAutoSetup() in onServiceConnected() after bootstrap extraction completes. Only runs on first-ever launch (checks ~/.nagato_ready).

## Grace Period
The Chat tab WebView retries connecting to localhost:8787 with exponential backoff:
- Try immediately
- Retry at 2s, 5s, 10s, 15s, 30s
- Show "Agent starting up..." during retries
- Show "Agent not running" after 60s of failures
