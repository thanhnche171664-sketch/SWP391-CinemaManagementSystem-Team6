import { useState } from 'react';
import { mockMovies } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Textarea } from '@/app/components/ui/textarea';
import { ArrowLeft, Star } from 'lucide-react';

interface ReviewPageProps {
  movieId: string;
  onBack: () => void;
  onSubmit: () => void;
}

export function ReviewPage({ movieId, onBack, onSubmit }: ReviewPageProps) {
  const [rating, setRating] = useState(0);
  const [hoveredRating, setHoveredRating] = useState(0);
  const [comment, setComment] = useState('');

  const movie = mockMovies.find((m) => m.id === movieId);

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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (rating === 0) {
      alert('Vui lòng chọn số sao đánh giá');
      return;
    }
    if (!comment.trim()) {
      alert('Vui lòng nhập nhận xét');
      return;
    }
    // Simulate review submission
    alert('Cảm ơn bạn đã đánh giá!');
    onSubmit();
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      {/* Back Button */}
      <Button variant="ghost" onClick={onBack} className="gap-2">
        <ArrowLeft className="w-4 h-4" />
        Quay lại
      </Button>

      <div>
        <h1 className="text-3xl font-bold mb-2">Đánh giá phim</h1>
        <p className="text-muted-foreground">Chia sẻ cảm nhận của bạn về bộ phim</p>
      </div>

      {/* Movie Info */}
      <Card className="p-6">
        <div className="flex gap-4">
          <img src={movie.poster} alt={movie.title} className="w-24 h-36 object-cover rounded" />
          <div>
            <h3 className="text-xl font-bold mb-2">{movie.title}</h3>
            <p className="text-sm text-muted-foreground">{movie.genre.join(', ')}</p>
            <p className="text-sm text-muted-foreground">{movie.duration} phút</p>
          </div>
        </div>
      </Card>

      {/* Review Form */}
      <Card className="p-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Rating */}
          <div>
            <label className="block font-semibold mb-3">Đánh giá của bạn *</label>
            <div className="flex items-center gap-2">
              {Array.from({ length: 5 }).map((_, i) => {
                const starValue = i + 1;
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => setRating(starValue)}
                    onMouseEnter={() => setHoveredRating(starValue)}
                    onMouseLeave={() => setHoveredRating(0)}
                    className="transition-transform hover:scale-110"
                  >
                    <Star
                      className={`w-10 h-10 ${
                        starValue <= (hoveredRating || rating)
                          ? 'fill-yellow-500 text-yellow-500'
                          : 'text-muted'
                      }`}
                    />
                  </button>
                );
              })}
              <span className="ml-3 text-lg font-semibold">
                {rating > 0 ? `${rating}/5` : 'Chọn số sao'}
              </span>
            </div>
          </div>

          {/* Comment */}
          <div>
            <label className="block font-semibold mb-3">Nhận xét của bạn *</label>
            <Textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Chia sẻ cảm nhận của bạn về bộ phim này..."
              rows={8}
              className="resize-none"
            />
            <p className="text-sm text-muted-foreground mt-2">
              {comment.length} / 500 ký tự
            </p>
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <Button type="submit" size="lg">
              Gửi đánh giá
            </Button>
            <Button type="button" variant="outline" size="lg" onClick={onBack}>
              Hủy
            </Button>
          </div>
        </form>
      </Card>

      {/* Tips */}
      <Card className="p-4 bg-muted/30">
        <h4 className="font-semibold mb-2">Gợi ý khi viết đánh giá:</h4>
        <ul className="text-sm text-muted-foreground space-y-1 list-disc list-inside">
          <li>Chia sẻ cảm nhận chân thật về bộ phim</li>
          <li>Đánh giá về kịch bản, diễn xuất, hình ảnh, âm nhạc...</li>
          <li>Không tiết lộ nội dung phim (spoiler)</li>
          <li>Sử dụng ngôn từ lịch sự, tôn trọng</li>
        </ul>
      </Card>
    </div>
  );
}
