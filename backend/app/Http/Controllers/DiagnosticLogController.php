<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Str;

class DiagnosticLogController extends Controller
{
    public function store(Request $request)
    {
        $data = $request->validate([
            'report' => ['required', 'string', 'max:65536'],
            'app_version' => ['nullable', 'string', 'max:40'],
            'device' => ['nullable', 'string', 'max:120'],
        ]);

        $token = (string) config('services.github.diagnostics_token', env('MARKETPG_DIAGNOSTICS_GITHUB_TOKEN', ''));
        $repository = (string) env('MARKETPG_DIAGNOSTICS_GITHUB_REPO', 'casndrih/MarketPG');
        if ($token === '') {
            return response()->json(['message' => 'Diagnostic uploads are not configured on the server yet.'], 503);
        }

        // Defense in depth: scrub common secrets even if a client sends an unredacted report.
        $report = preg_replace('/(?i)(authorization\s*[:=]\s*bearer\s+)[^\s]+/', '$1[REDACTED]', $data['report']);
        $report = preg_replace('/(?i)((?:password|passwd|token|secret|api[_-]?key)\s*[:=]\s*)[^\s,;]+/', '$1[REDACTED]', $report);
        $report = mb_substr($report, 0, 60000);

        $now = now()->utc();
        $path = 'debug-logs/' . $now->format('Y/m/d') . '/' . $now->format('His') . '-' . Str::lower(Str::random(8)) . '.txt';
        $content = "MarketPG Android diagnostic report\n"
            . "Received UTC: " . $now->toIso8601String() . "\n"
            . "User ID: " . (string) $request->user()->getAuthIdentifier() . "\n"
            . "App version: " . ($data['app_version'] ?? 'unknown') . "\n"
            . "Device: " . ($data['device'] ?? 'unknown') . "\n\n"
            . $report . "\n";

        $response = Http::withToken($token)
            ->acceptJson()
            ->asJson()
            ->timeout(15)
            ->put('https://api.github.com/repos/' . $repository . '/contents/' . $path, [
                'message' => 'Add Android diagnostic report',
                'content' => base64_encode($content),
            ]);

        if (!$response->successful()) {
            report(new \RuntimeException('GitHub diagnostic upload failed with HTTP ' . $response->status()));
            return response()->json(['message' => 'GitHub could not accept the diagnostic report. Please try again later.'], 502);
        }

        return response()->json([
            'message' => 'Diagnostic report uploaded.',
            'path' => $path,
            'url' => $response->json('content.html_url'),
        ], 201);
    }
}
