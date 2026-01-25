import { useState } from 'react';
import { mockShowtimes, mockMovies, mockCinemas } from '@/data/mockData';
import { Button } from '@/app/components/ui/button';
import { Card } from '@/app/components/ui/card';
import { Input } from '@/app/components/ui/input';
import { Label } from '@/app/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/app/components/ui/radio-group';
import { Separator } from '@/app/components/ui/separator';
import { ArrowLeft, CreditCard, Smartphone, Wallet } from 'lucide-react';

interface PaymentPageProps {
  showtimeId: string;
  selectedSeats: string[];
  totalPrice: number;
  onBack: () => void;
  onConfirm: (bookingId: string) => void;
}

export function PaymentPage({
  showtimeId,
  selectedSeats,
  totalPrice,
  onBack,
  onConfirm,
}: PaymentPageProps) {
  const [paymentMethod, setPaymentMethod] = useState('credit-card');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');

  const showtime = mockShowtimes.find((s) => s.id === showtimeId);
  const movie = mockMovies.find((m) => m.id === showtime?.movieId);
  const cinema = mockCinemas.find((c) => c.id === showtime?.cinemaId);
  const room = cinema?.rooms.find((r) => r.id === showtime?.roomId);

  if (!showtime || !movie || !cinema || !room) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin</p>
        <Button onClick={onBack} className="mt-4">
          Quay lại
        </Button>
      </div>
    );
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !phone) {
      alert('Vui lòng điền đầy đủ thông tin');
      return;
    }
    // Simulate booking creation
    const bookingId = 'b' + Date.now();
    onConfirm(bookingId);
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Back Button */}
      <Button variant="ghost" onClick={onBack} className="gap-2">
        <ArrowLeft className="w-4 h-4" />
        Quay lại
      </Button>

      <h1 className="text-3xl font-bold">Thanh toán</h1>

      <form onSubmit={handleSubmit}>
        <div className="grid lg:grid-cols-[1fr,400px] gap-6">
          {/* Payment Form */}
          <div className="space-y-6">
            {/* Customer Info */}
            <Card className="p-6">
              <h3 className="font-semibold mb-4">Thông tin khách hàng</h3>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="name">Họ và tên *</Label>
                  <Input
                    id="name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Nhập họ và tên"
                    required
                  />
                </div>
                <div>
                  <Label htmlFor="email">Email *</Label>
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="example@email.com"
                    required
                  />
                </div>
                <div>
                  <Label htmlFor="phone">Số điện thoại *</Label>
                  <Input
                    id="phone"
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="0912345678"
                    required
                  />
                </div>
              </div>
            </Card>

            {/* Payment Method */}
            <Card className="p-6">
              <h3 className="font-semibold mb-4">Phương thức thanh toán</h3>
              <RadioGroup value={paymentMethod} onValueChange={setPaymentMethod}>
                <div className="space-y-3">
                  <div className="flex items-center space-x-3 p-4 rounded-lg border cursor-pointer hover:bg-accent">
                    <RadioGroupItem value="credit-card" id="credit-card" />
                    <Label htmlFor="credit-card" className="flex items-center gap-3 cursor-pointer flex-1">
                      <CreditCard className="w-5 h-5" />
                      <div>
                        <p className="font-medium">Thẻ tín dụng / Ghi nợ</p>
                        <p className="text-sm text-muted-foreground">Visa, Mastercard, JCB</p>
                      </div>
                    </Label>
                  </div>

                  <div className="flex items-center space-x-3 p-4 rounded-lg border cursor-pointer hover:bg-accent">
                    <RadioGroupItem value="momo" id="momo" />
                    <Label htmlFor="momo" className="flex items-center gap-3 cursor-pointer flex-1">
                      <Smartphone className="w-5 h-5" />
                      <div>
                        <p className="font-medium">Ví MoMo</p>
                        <p className="text-sm text-muted-foreground">Thanh toán qua ví điện tử MoMo</p>
                      </div>
                    </Label>
                  </div>

                  <div className="flex items-center space-x-3 p-4 rounded-lg border cursor-pointer hover:bg-accent">
                    <RadioGroupItem value="zalopay" id="zalopay" />
                    <Label htmlFor="zalopay" className="flex items-center gap-3 cursor-pointer flex-1">
                      <Wallet className="w-5 h-5" />
                      <div>
                        <p className="font-medium">ZaloPay</p>
                        <p className="text-sm text-muted-foreground">Thanh toán qua ví ZaloPay</p>
                      </div>
                    </Label>
                  </div>
                </div>
              </RadioGroup>

              {paymentMethod === 'credit-card' && (
                <div className="mt-4 space-y-4 pt-4 border-t">
                  <div>
                    <Label htmlFor="card-number">Số thẻ</Label>
                    <Input id="card-number" placeholder="1234 5678 9012 3456" />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="expiry">Ngày hết hạn</Label>
                      <Input id="expiry" placeholder="MM/YY" />
                    </div>
                    <div>
                      <Label htmlFor="cvv">CVV</Label>
                      <Input id="cvv" placeholder="123" />
                    </div>
                  </div>
                </div>
              )}
            </Card>
          </div>

          {/* Order Summary */}
          <Card className="p-6 h-fit sticky top-6">
            <h3 className="font-semibold mb-4">Chi tiết đơn hàng</h3>

            <div className="space-y-4">
              <div className="flex gap-3">
                <img src={movie.poster} alt={movie.title} className="w-20 h-28 object-cover rounded" />
                <div className="flex-1 text-sm">
                  <p className="font-semibold mb-1">{movie.title}</p>
                  <p className="text-muted-foreground">{cinema.name}</p>
                  <p className="text-muted-foreground">{room.name}</p>
                  <p className="text-muted-foreground">
                    {showtime.date} - {showtime.time}
                  </p>
                </div>
              </div>

              <Separator />

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Ghế:</span>
                  <span className="font-medium">{selectedSeats.join(', ')}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Số lượng:</span>
                  <span className="font-medium">{selectedSeats.length} ghế</span>
                </div>
              </div>

              <Separator />

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Tạm tính:</span>
                  <span>{totalPrice.toLocaleString('vi-VN')}đ</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Phí dịch vụ:</span>
                  <span>0đ</span>
                </div>
                <Separator />
                <div className="flex justify-between text-lg">
                  <span className="font-semibold">Tổng cộng:</span>
                  <span className="font-bold text-primary">
                    {totalPrice.toLocaleString('vi-VN')}đ
                  </span>
                </div>
              </div>

              <Button type="submit" className="w-full" size="lg">
                Xác nhận thanh toán
              </Button>

              <p className="text-xs text-muted-foreground text-center">
                Bằng việc nhấn "Xác nhận thanh toán", bạn đồng ý với{' '}
                <a href="#" className="underline">
                  Điều khoản dịch vụ
                </a>{' '}
                của chúng tôi
              </p>
            </div>
          </Card>
        </div>
      </form>
    </div>
  );
}
