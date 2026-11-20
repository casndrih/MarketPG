<?php

namespace App\Notifications;

use App\Models\Listing;
use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

class FollowedSellerNewListing extends Notification
{
    use Queueable;

    public function __construct(public Listing $listing) {}

    public function via(object $notifiable): array
    {
        return ['database', 'mail'];
    }

    public function toDatabase(object $notifiable): array
    {
        return [
            'kind' => 'seller_new_listing',
            'seller_id' => $this->listing->user_id,
            'listing_id' => $this->listing->id,
            'title' => $this->listing->title,
            'message' => $this->listing->user?->name . ' published a new item.',
            'url' => url('/listing/' . $this->listing->id),
        ];
    }

    public function toMail(object $notifiable): MailMessage
    {
        return (new MailMessage)
            ->subject('New item from a seller you follow on MarketPG')
            ->greeting('Hello ' . $notifiable->name . ',')
            ->line(($this->listing->user?->name ?? 'A seller you follow') . ' has published a new item: ' . $this->listing->title)
            ->action('View listing', url('/listing/' . $this->listing->id))
            ->line('You are receiving this because you follow this seller on MarketPG.');
    }
}
