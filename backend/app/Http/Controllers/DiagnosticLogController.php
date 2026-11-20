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

        $token = (string) config('marketpg.diagnostics_github_token', '');
        $repository = (string) config('marketpg.diagnostics_github_repo', 'casndrih/MarketPG');
        if ($token === '' || !preg_match('/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/', $repository)) {
            return response()->json(['message' => 'Diagnostic uploads are not configured on the server yet.'], 503);
        }

        // Defense in depth: scrub common secrets even if a client sends an unredacted report.
        $report = preg_replace('/(?i)(authorization\s*[:=]\s*bearer\s+)[^\s]+/', '$1[REDACTED]', $data['report']);
        $report = preg_replace('/(?i)((?:password|passwd|token|secret|api[_-]?key)\s*[:=]\s*)[^\s,;]+/', '$1[REDACTED]', $report);
        $report = preg_replace('/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}/', '[EMAIL]', $report);
        $report = preg_replace('/(?<![A-Za-z0-9])\\+?[0-9][0-9 ()-]{6,}[0-9](?![A-Za-z0-9])/', '[PHONE/NUMBER]', $report);
        $report = mb_substr($report, 0, 60000);

        $now = now()->utc();
        $path = 'debug-logs/' . $now->format('Y/m/d') . '/' . $now->format('His') . '-' . Str::lower(Str::random(8)) . '.txt';
        $content = "MarketPG Android diagnostic report\n"
            . "Received UTC: " . $now->toIso8601String() . "\n"
            . "App version: " . ($data['app_version'] ?? 'unknown') . "\n"
            . "Device: " . (preg_match('/^Android [0-9.]+; SDK [0-9]+$/', (string) ($data['device'] ?? '')) ? $data['device'] : 'Android version unavailable') . "\n\n"
            . $report . "\n";

        $response = Http::withToken($token)
            ->withHeaders(['User-Agent' => 'MarketPG-Android-Diagnostics', 'X-GitHub-Api-Version' => '2022-11-28'])
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
