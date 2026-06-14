package server

import (
	"encoding/json"
	"html/template"
	"net/http"
	"time"
)

type Config struct {
	Version string
	Now     func() time.Time
}

type app struct {
	version string
	now     func() time.Time
}

type healthResponse struct {
	Status  string `json:"status"`
	Service string `json:"service"`
	Version string `json:"version"`
}

type timeResponse struct {
	Unix    int64  `json:"unix"`
	RFC3339 string `json:"rfc3339"`
}

type homeData struct {
	Service string
	Version string
	Time    string
}

func New(cfg Config) http.Handler {
	if cfg.Version == "" {
		cfg.Version = "dev"
	}
	if cfg.Now == nil {
		cfg.Now = time.Now
	}

	a := &app{
		version: cfg.Version,
		now:     cfg.Now,
	}

	mux := http.NewServeMux()
	mux.HandleFunc("GET /", a.handleHome)
	mux.HandleFunc("GET /healthz", a.handleHealth)
	mux.HandleFunc("GET /api/time", a.handleTime)

	return mux
}

func (a *app) handleHome(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		http.NotFound(w, r)
		return
	}

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(http.StatusOK)

	_ = homeTemplate.Execute(w, homeData{
		Service: "Go Demo",
		Version: a.version,
		Time:    a.now().UTC().Format(time.RFC3339),
	})
}

func (a *app) handleHealth(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, healthResponse{
		Status:  "ok",
		Service: "go-demo",
		Version: a.version,
	})
}

func (a *app) handleTime(w http.ResponseWriter, _ *http.Request) {
	now := a.now().UTC()
	writeJSON(w, http.StatusOK, timeResponse{
		Unix:    now.Unix(),
		RFC3339: now.Format(time.RFC3339),
	})
}

func writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

var homeTemplate = template.Must(template.New("home").Parse(`<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Go Demo</title>
    <style>
      :root {
        color-scheme: light dark;
        --bg: #f5f7fb;
        --ink: #172033;
        --muted: #687187;
        --panel: #ffffff;
        --line: #dfe5ef;
        --accent: #1f8f6a;
      }

      @media (prefers-color-scheme: dark) {
        :root {
          --bg: #111827;
          --ink: #f8fafc;
          --muted: #a7b0c0;
          --panel: #182235;
          --line: #2c3a52;
          --accent: #4ade80;
        }
      }

      * {
        box-sizing: border-box;
      }

      body {
        min-height: 100vh;
        margin: 0;
        display: grid;
        place-items: center;
        padding: 32px;
        background: var(--bg);
        color: var(--ink);
        font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      }

      main {
        width: min(720px, 100%);
        border: 1px solid var(--line);
        border-radius: 8px;
        background: var(--panel);
        padding: 28px;
        box-shadow: 0 22px 70px rgba(15, 23, 42, 0.12);
      }

      p {
        color: var(--muted);
        line-height: 1.7;
      }

      h1 {
        margin: 0;
        font-size: clamp(2.2rem, 8vw, 4.5rem);
        line-height: 0.95;
        letter-spacing: 0;
      }

      dl {
        display: grid;
        grid-template-columns: max-content 1fr;
        gap: 12px 18px;
        margin: 28px 0 0;
      }

      dt {
        color: var(--muted);
        font-weight: 700;
      }

      dd {
        margin: 0;
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      }

      a {
        color: var(--accent);
        font-weight: 700;
      }
    </style>
  </head>
  <body>
    <main>
      <h1>{{.Service}}</h1>
      <p>A small Go HTTP service running with only the standard library.</p>
      <dl>
        <dt>Version</dt>
        <dd>{{.Version}}</dd>
        <dt>Server time</dt>
        <dd>{{.Time}}</dd>
        <dt>Health</dt>
        <dd><a href="/healthz">/healthz</a></dd>
        <dt>API</dt>
        <dd><a href="/api/time">/api/time</a></dd>
      </dl>
    </main>
  </body>
</html>`))
