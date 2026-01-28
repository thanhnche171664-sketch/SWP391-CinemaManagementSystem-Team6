import { mockMovies, mockReviews } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Badge } from '@/app/components/ui/badge';
import { Card } from '@/app/components/ui/card';
import { Separator } from '@/app/components/ui/separator';
import { Star, Clock, Calendar, ArrowLeft, Play } from 'lucide-react';
import { ShowtimesPage } from '@/app/pages/ShowtimesPage';
import { useState } from 'react';

interface MovieDetailPageProps {
  movieId: string;
  onBack: () => void;
  onBooking: (movieId: string) => void;
  onReview: (movieId: string) => void;
  onSelectShowtime?: (showtimeId: string) => void;
}

export function MovieDetailPage({ movieId, onBack, onBooking, onReview, onSelectShowtime }: MovieDetailPageProps) {
  const [showBooking, setShowBooking] = useState(false);
  const movie = mockMovies.find((m) => m.id === movieId);
  const reviews = mockReviews.filter((r) => r.movieId === movieId);

  if (!movie) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy phim</p>
        <Button onClick={onBack} className="mt-4">
          Quay lại
        </Button>
      </div>
    );
  }

  // If showing booking, display ShowtimesPage
  if (showBooking) {
    return (
      <div>
        <Button variant="ghost" onClick={() => setShowBooking(false)} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          Quay lại thông tin phim
        </Button>
        <ShowtimesPage 
          movieId={movieId} 
          onSelectShowtime={(showtimeId) => {
            if (onSelectShowtime) {
              onSelectShowtime(showtimeId);
            }
          }} 
        />
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Back Button */}
      <Button variant="ghost" onClick={onBack} className="gap-2">
        <ArrowLeft className="w-4 h-4" />
        Quay lại
      </Button>

      {/* Hero Section */}
      <div className="relative h-[300px] md:h-[500px] rounded-2xl overflow-hidden">
        <img
          src={movie.backdrop}
          alt={movie.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
      </div>

      {/* Movie Info */}
      <div className="grid md:grid-cols-[300px,1fr] gap-8">
        {/* Poster */}
        <div className="hidden md:block">
          <img
            src={movie.poster}
            alt={movie.title}
            className="w-full rounded-xl shadow-2xl"
          />
        </div>

        {/* Details */}
        <div className="space-y-6">
          <div>
            <div className="flex flex-wrap items-center gap-3 mb-3">
              <Badge variant={movie.status === 'now-showing' ? 'default' : 'secondary'}>
                {movie.status === 'now-showing' ? 'Đang chiếu' : 'Sắp chiếu'}
              </Badge>
              <Badge variant="outline">{movie.ageRating}</Badge>
            </div>
            <h1 className="text-3xl md:text-5xl font-bold mb-2">{movie.title}</h1>
            <p className="text-muted-foreground">{movie.originalTitle}</p>
          </div>

          <div className="flex flex-wrap gap-6 text-sm">
            <div className="flex items-center gap-2">
              <Star className="w-5 h-5 fill-yellow-500 text-yellow-500" />
              <span className="text-lg font-semibold">{movie.rating}</span>
              <span className="text-muted-foreground">/ 10</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5" />
              <span>{movie.duration} phút</span>
            </div>
            <div className="flex items-center gap-2">
              <Calendar className="w-5 h-5" />
              <span>{new Date(movie.releaseDate).toLocaleDateString('vi-VN')}</span>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {movie.genre.map((g) => (
              <Badge key={g} variant="outline">
                {g}
              </Badge>
            ))}
          </div>

          <div className="flex gap-3">
            <Button onClick={() => setShowBooking(true)} className="gap-2" size="lg">
              Đặt vé ngay
            </Button>
            <Button variant="outline" size="lg" className="gap-2">
              <Play className="w-4 h-4" />
              Xem trailer
            </Button>
          </div>

          <Separator />

          <div className="space-y-4">
            <div>
              <h3 className="font-semibold mb-2">Nội dung phim</h3>
              <p className="text-muted-foreground leading-relaxed">{movie.description}</p>
            </div>

            <div>
              <h3 className="font-semibold mb-2">Đạo diễn</h3>
              <p className="text-muted-foreground">{movie.director}</p>
            </div>

            <div>
              <h3 className="font-semibold mb-2">Diễn viên</h3>
              <p className="text-muted-foreground">{movie.cast.join(', ')}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Reviews Section */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Đánh giá ({reviews.length})</h2>
          <Button onClick={() => onReview(movieId)} variant="outline">
            Viết đánh giá
          </Button>
        </div>

        <div className="grid gap-4">
          {reviews.map((review) => (
            <Card key={review.id} className="p-6">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="font-semibold">{review.userName}</p>
                  <p className="text-sm text-muted-foreground">
                    {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                  </p>
                </div>
                <div className="flex items-center gap-1">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < review.rating
                          ? 'fill-yellow-500 text-yellow-500'
                          : 'text-muted'
                      }`}
                    />
                  ))}
                </div>
              </div>
              <p className="text-muted-foreground">{review.comment}</p>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}