<?php

namespace App\Http\Controllers;

use App\Models\IdentityVerification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;

class IdentityVerificationController extends Controller
{
    public function status(Request $request)
    {
        $verification = IdentityVerification::where('user_id', $request->user()->id)->latest()->first();
        return response()->json(['data' => $verification]);
    }

    public function submit(Request $request)
    {
        abort_unless(config('marketpg.identity_uploads_enabled'), 503, 'Identity document submissions are temporarily unavailable while secure private storage is being configured.');
        $data = $request->validate([
            'document_type' => ['required', Rule::in(['png_nid', 'passport', 'drivers_licence', 'other_government_id'])],
            'document_number' => ['nullable', 'string', 'max:80'],
            'id_front' => ['required', 'file', 'mimes:jpg,jpeg,png,pdf', 'max:8192'],
            'id_back' => ['nullable', 'file', 'mimes:jpg,jpeg,png,pdf', 'max:8192'],
            'selfie' => ['required', 'image', 'mimes:jpg,jpeg,png', 'max:8192'],
            'consent' => ['required', 'accepted'],
        ]);

        $existing = IdentityVerification::where('user_id', $request->user()->id)
            ->where('status', 'pending')->exists();
        if ($existing) {
            return response()->json(['message' => 'You already have a verification awaiting review.'], 409);
        }

        $front = $request->file('id_front')->store('identity-verifications', config('marketpg.private_disk'));
        $back = $request->hasFile('id_back') ? $request->file('id_back')->store('identity-verifications', 'local') : null;
        $selfie = $request->file('selfie')->store('identity-verifications', 'local');

        $number = preg_replace('/\s+/', '', (string) ($data['document_number'] ?? ''));
        $verification = IdentityVerification::create([
            'user_id' => $request->user()->id,
            'document_type' => $data['document_type'],
            'document_last_four' => $number !== '' ? substr($number, -4) : null,
            'front_path' => $front,
            'back_path' => $back,
            'selfie_path' => $selfie,
            'status' => 'pending',
            'consented_at' => now(),
        ]);

        return response()->json([
            'message' => 'Identity documents submitted for manual review.',
            'data' => $verification,
        ], 201);
    }

    public function queue()
    {
        return response()->json(['data' => IdentityVerification::with('user:id,name,email')->where('status', 'pending')->latest()->paginate(25)]);
    }

    public function review(Request $request, IdentityVerification $verification)
    {
        abort_unless($verification->status === 'pending', 409, 'Only pending verifications can be reviewed.');
        $data = $request->validate([
            'decision' => ['required', Rule::in(['approved', 'rejected'])],
            'reason' => ['nullable', 'string', 'max:1000'],
        ]);
        if ($data['decision'] === 'rejected' && empty(trim((string) ($data['reason'] ?? '')))) {
            return response()->json(['message' => 'A reason is required when rejecting a verification.'], 422);
        }
        $verification->update([
            'status' => $data['decision'],
            'rejection_reason' => $data['decision'] === 'rejected' ? $data['reason'] : null,
            'reviewed_by' => $request->user()->id,
            'reviewed_at' => now(),
        ]);
        return response()->json(['message' => 'Verification decision saved.', 'data' => $verification->fresh()]);
    }

    public function document(Request $request, IdentityVerification $verification, string $kind)
    {
        abort_unless(in_array($kind, ['front', 'back', 'selfie'], true), 404);
        $path = match ($kind) {
            'front' => $verification->front_path,
            'back' => $verification->back_path,
            'selfie' => $verification->selfie_path,
        };
        abort_unless($path && Storage::disk(config('marketpg.private_disk'))->exists($path), 404);
        return Storage::disk(config('marketpg.private_disk'))->download($path);
    }
}
