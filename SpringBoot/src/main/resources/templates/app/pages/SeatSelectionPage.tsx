import { useState } from 'react';
import { mockShowtimes, mockMovies, mockCinemas } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Badge } from '@/app/components/ui/badge';
import { Separator } from '@/app/components/ui/separator';
import { ArrowLeft, Armchair } from 'lucide-react';

interface SeatSelectionPageProps {
  showtimeId: string;
  onBack: () => void;
  onConfirm: (seats: string[], totalPrice: number) => void;
}

export function SeatSelectionPage({ showtimeId, onBack, onConfirm }: SeatSelectionPageProps) {
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);

  const showtime = mockShowtimes.find((s) => s.id === showtimeId);
  const movie = mockMovies.find((m) => m.id === showtime?.movieId);
  const cinema = mockCinemas.find((c) => c.id === showtime?.cinemaId);
  const room = cinema?.rooms.find((r) => r.id === showtime?.roomId);

  if (!showtime || !movie || !cinema || !room) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin suất chiếu</p>
        <Button onClick={onBack} className="mt-4">
          Quay lại
        </Button>
      </div>
    );
  }

  const handleSeatClick = (seatId: string) => {
    const seat = room.seats.find((s) => s.id === seatId);
    if (!seat || seat.status === 'sold') return;

    if (selectedSeats.includes(seatId)) {
      setSelectedSeats(selectedSeats.filter((s) => s !== seatId));
    } else {
      if (selectedSeats.length >= 10) {
        alert('Bạn chỉ có thể chọn tối đa 10 ghế');
        return;
      }
      setSelectedSeats([...selectedSeats, seatId]);
    }
  };

  const totalPrice = selectedSeats.reduce((sum, seatId) => {
    const seat = room.seats.find((s) => s.id === seatId);
    return sum + (seat?.price || 0);
  }, 0);

  const rows = Array.from(new Set(room.seats.map((s) => s.row))).sort();

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Back Button */}
      <Button variant="ghost" onClick={onBack} className="gap-2">
        <ArrowLeft className="w-4 h-4" />
        Quay lại
      </Button>

      {/* Movie Info */}
      <Card className="p-6">
        <div className="flex gap-4">
          <img src={movie.poster} alt={movie.title} className="w-24 h-36 object-cover rounded" />
          <div className="flex-1">
            <h2 className="text-2xl font-bold mb-2">{movie.title}</h2>
            <div className="space-y-1 text-sm text-muted-foreground">
              <p>
                <strong>Rạp:</strong> {cinema.name}
              </p>
              <p>
                <strong>Phòng:</strong> {room.name}
              </p>
              <p>
                <strong>Suất chiếu:</strong> {showtime.time} - {showtime.date}
              </p>
            </div>
          </div>
        </div>
      </Card>

      <div className="grid lg:grid-cols-[1fr,350px] gap-6">
        {/* Seat Map */}
        <Card className="p-6">
          <div className="space-y-6">
            {/* Screen */}
            <div className="text-center">
              <div className="inline-block px-12 py-3 bg-gradient-to-b from-primary/20 to-transparent rounded-t-full border-t-2 border-primary">
                <p className="text-sm font-semibold">MÀN HÌNH</p>
              </div>
            </div>

            {/* Seats */}
            <div className="space-y-3 overflow-x-auto">
              {rows.map((row) => {
                const rowSeats = room.seats.filter((s) => s.row === row);
                return (
                  <div key={row} className="flex items-center gap-2 justify-center">
                    <span className="w-8 text-center font-semibold text-sm">{row}</span>
                    <div className="flex gap-2">
                      {rowSeats.map((seat) => {
                        const isSelected = selectedSeats.includes(seat.id);
                        const isSold = seat.status === 'sold';

                        return (
                          <button
                            key={seat.id}
                            onClick={() => handleSeatClick(seat.id)}
                            disabled={isSold}
                            className={`w-8 h-8 rounded-t-lg flex items-center justify-center text-xs transition-all ${
                              isSold
                                ? 'bg-muted text-muted-foreground cursor-not-allowed'
                                : isSelected
                                ? 'bg-primary text-primary-foreground shadow-lg scale-110'
                                : seat.type === 'vip'
                                ? 'bg-amber-600/20 border-2 border-amber-600 hover:bg-amber-600/30'
                                : seat.type === 'couple'
                                ? 'bg-pink-600/20 border-2 border-pink-600 hover:bg-pink-600/30'
                                : 'bg-accent hover:bg-accent/80'
                            }`}
                          >
                            <Armchair className="w-4 h-4" />
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Legend */}
            <div className="flex flex-wrap gap-6 justify-center pt-4 border-t">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-accent rounded-t-lg flex items-center justify-center">
                  <Armchair className="w-4 h-4" />
                </div>
                <span className="text-sm">Ghế thường</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-amber-600/20 border-2 border-amber-600 rounded-t-lg flex items-center justify-center">
                  <Armchair className="w-4 h-4" />
                </div>
                <span className="text-sm">Ghế VIP</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-pink-600/20 border-2 border-pink-600 rounded-t-lg flex items-center justify-center">
                  <Armchair className="w-4 h-4" />
                </div>
                <span className="text-sm">Ghế đôi</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-primary rounded-t-lg flex items-center justify-center">
                  <Armchair className="w-4 h-4 text-primary-foreground" />
                </div>
                <span className="text-sm">Đang chọn</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 bg-muted rounded-t-lg flex items-center justify-center">
                  <Armchair className="w-4 h-4" />
                </div>
                <span className="text-sm">Đã bán</span>
              </div>
            </div>
          </div>
        </Card>

        {/* Booking Summary */}
        <Card className="p-6 h-fit sticky top-6">
          <h3 className="font-semibold mb-4">Thông tin đặt vé</h3>

          <div className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Số ghế đã chọn:</span>
              <span className="font-semibold">{selectedSeats.length}</span>
            </div>

            {selectedSeats.length > 0 && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">Ghế:</span>
                <span className="font-semibold">{selectedSeats.join(', ')}</span>
              </div>
            )}

            <Separator />

            <div className="flex justify-between text-lg">
              <span className="font-semibold">Tổng tiền:</span>
              <span className="font-bold text-primary">
                {totalPrice.toLocaleString('vi-VN')}đ
              </span>
            </div>
          </div>

          <Button
            onClick={() => onConfirm(selectedSeats, totalPrice)}
            disabled={selectedSeats.length === 0}
            className="w-full mt-6"
            size="lg"
          >
            Tiếp tục
          </Button>
        </Card>
      </div>
    </div>
  );
}
