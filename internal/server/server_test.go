package server

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

func TestHealth(t *testing.T) {
	handler := New(Config{Version: "test"})
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/healthz", nil)

	handler.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, recorder.Code)
	}

	var body healthResponse
	if err := json.NewDecoder(recorder.Body).Decode(&body); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if body.Status != "ok" || body.Service != "go-demo" || body.Version != "test" {
		t.Fatalf("unexpected response: %+v", body)
	}
}

func TestTimeUsesInjectedClock(t *testing.T) {
	fixed := time.Date(2026, 6, 14, 12, 0, 0, 0, time.UTC)
	handler := New(Config{
		Version: "test",
		Now: func() time.Time {
			return fixed
		},
	})
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/api/time", nil)

	handler.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, recorder.Code)
	}

	var body timeResponse
	if err := json.NewDecoder(recorder.Body).Decode(&body); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if body.Unix != fixed.Unix() || body.RFC3339 != "2026-06-14T12:00:00Z" {
		t.Fatalf("unexpected response: %+v", body)
	}
}

func TestHome(t *testing.T) {
	handler := New(Config{Version: "test"})
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/", nil)

	handler.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, recorder.Code)
	}
	if !strings.Contains(recorder.Body.String(), "Go Demo") {
		t.Fatal("expected home page to include service name")
	}
}

func TestNotFound(t *testing.T) {
	handler := New(Config{Version: "test"})
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/missing", nil)

	handler.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusNotFound {
		t.Fatalf("expected status %d, got %d", http.StatusNotFound, recorder.Code)
	}
}
