import { useState } from 'react';
import { mockMovies, mockShowtimes, mockCinemas } from '@/data/mockData';
import { Card } from '@/app/components/ui/card';
import { Button } from '@/app/components/ui/button';
import { Badge } from '@/app/components/ui/badge';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/app/components/ui/tabs';
import { MapPin, Clock, Armchair } from 'lucide-react';

interface ShowtimesPageProps {
  movieId?: string;
  onSelectShowtime: (showtimeId: string) => void;
}

export function ShowtimesPage({ movieId, onSelectShowtime }: ShowtimesPageProps) {
  const [selectedCinema, setSelectedCinema] = useState<string | null>(null);

  const movie = mockMovies.find((m) => m.id === movieId);

  // Filter showtimes by movie
  const movieShowtimes = mockShowtimes.filter((st) => st.movieId === movieId);

  // Get cinemas that have this movie
  const availableCinemas = mockCinemas.filter((cinema) =>
    movieShowtimes.some((st) => st.cinemaId === cinema.id)
  );

  // Set default cinema if not selected
  if (!selectedCinema && availableCinemas.length > 0) {
    setSelectedCinema(availableCinemas[0].id);
  }

  // Filter showtimes by selected cinema
  const filteredShowtimes = selectedCinema
    ? movieShowtimes.filter((st) => st.cinemaId === selectedCinema)
    : [];

  // Group showtimes by date
  const showtimesByDate = filteredShowtimes.reduce((acc, showtime) => {
    if (!acc[showtime.date]) {
      acc[showtime.date] = [];
    }
    acc[showtime.date].push(showtime);
    return acc;
  }, {} as Record<string, typeof filteredShowtimes>);

  const selectedCinemaData = mockCinemas.find((c) => c.id === selectedCinema);

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold mb-2">
          {movie ? `Chọn suất chiếu - ${movie.title}` : 'Chọn suất chiếu'}
        </h1>
        <p className="text-muted-foreground">Chọn rạp chiếu và suất chiếu phù hợp với bạn</p>
      </div>

      {/* Cinema Selector */}
      <div>
        <h2 className="text-xl font-semibold mb-4">Chọn rạp chiếu</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {availableCinemas.map((cinema) => {
            const isSelected = cinema.id === selectedCinema;
            return (
              <Card
                key={cinema.id}
                onClick={() => setSelectedCinema(cinema.id)}
                className={`p-4 cursor-pointer transition-all hover:border-primary ${
                  isSelected ? 'bg-primary/10 border-primary' : ''
                }`}
              >
                <h3 className="font-semibold mb-2">{cinema.name}</h3>
                <div className="flex items-start gap-2 text-sm text-muted-foreground">
                  <MapPin className="w-4 h-4 mt-0.5 flex-shrink-0" />
                  <span>{cinema.address}</span>
                </div>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Showtimes by Date */}
      {selectedCinema && (
        <div className="space-y-6">
          <div>
            <h2 className="text-xl font-semibold mb-2">
              Suất chiếu tại {selectedCinemaData?.name}
            </h2>
            <p className="text-sm text-muted-foreground mb-4">
              Chọn ngày và giờ chiếu
            </p>
          </div>

          {Object.keys(showtimesByDate).length > 0 ? (
            <div className="space-y-6">
              {Object.entries(showtimesByDate).map(([date, showtimes]) => {
                const dateObj = new Date(date);
                const dayName = dateObj.toLocaleDateString('vi-VN', {
                  weekday: 'long',
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric',
                });

                return (
                  <Card key={date} className="p-6">
                    <h3 className="font-semibold text-lg mb-4 capitalize">{dayName}</h3>
                    <div className="flex flex-wrap gap-3">
                      {showtimes.map((showtime) => {
                        const room = selectedCinemaData?.rooms.find(
                          (r) => r.id === showtime.roomId
                        );
                        return (
                          <Button
                            key={showtime.id}
                            variant="outline"
                            onClick={() => onSelectShowtime(showtime.id)}
                            className="flex flex-col items-center h-auto p-4 hover:bg-primary hover:text-primary-foreground transition-colors min-w-[100px]"
                          >
                            <span className="font-semibold text-lg">{showtime.time}</span>
                            <span className="text-xs opacity-80 mt-1">{room?.name}</span>
                            <div className="flex items-center gap-1 text-xs mt-2">
                              <Armchair className="w-3 h-3" />
                              <span>{showtime.availableSeats} ghế</span>
                            </div>
                          </Button>
                        );
                      })}
                    </div>
                  </Card>
                );
              })}
            </div>
          ) : (
            <Card className="p-12 text-center">
              <p className="text-muted-foreground">Không có suất chiếu nào tại rạp này</p>
            </Card>
          )}
        </div>
      )}
    </div>
  );
}