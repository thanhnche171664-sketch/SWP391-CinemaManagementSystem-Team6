import { Movie } from '@/types';
import { Star, Clock } from 'lucide-react';
import { Card } from '@/app/components/ui/card';
import { Badge } from '@/app/components/ui/badge';

interface MovieCardProps {
  movie: Movie;
  onClick: () => void;
}

export function MovieCard({ movie, onClick }: MovieCardProps) {
  return (
    <Card
      onClick={onClick}
      className="group cursor-pointer overflow-hidden border-border hover:border-primary/50 transition-all duration-300 bg-card"
    >
      <div className="relative aspect-[2/3] overflow-hidden">
        <img
          src={movie.poster}
          alt={movie.title}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
        />
        <div className="absolute top-2 right-2">
          <Badge
            variant={
              movie.status === 'now-showing'
                ? 'default'
                : movie.status === 'coming-soon'
                ? 'secondary'
                : 'outline'
            }
            className="bg-background/80 backdrop-blur"
          >
            {movie.status === 'now-showing'
              ? 'Đang chiếu'
              : movie.status === 'coming-soon'
              ? 'Sắp chiếu'
              : 'Đã kết thúc'}
          </Badge>
        </div>
      </div>

      <div className="p-4 space-y-2">
        <h3 className="font-semibold line-clamp-1 group-hover:text-primary transition-colors">
          {movie.title}
        </h3>

        <div className="flex items-center gap-4 text-sm text-muted-foreground">
          <div className="flex items-center gap-1">
            <Star className="w-4 h-4 fill-yellow-500 text-yellow-500" />
            <span>{movie.rating}</span>
          </div>
          <div className="flex items-center gap-1">
            <Clock className="w-4 h-4" />
            <span>{movie.duration} phút</span>
          </div>
        </div>

        <div className="flex flex-wrap gap-1">
          {movie.genre.slice(0, 2).map((g) => (
            <Badge key={g} variant="outline" className="text-xs">
              {g}
            </Badge>
          ))}
        </div>
      </div>
    </Card>
  );
}
