import { Movie, Cinema, Room, Seat, Showtime, Booking, Ticket, Review, DashboardStats } from '@/types';

export const mockMovies: Movie[] = [
  {
    id: '1',
    title: 'Oppenheimer',
    originalTitle: 'Oppenheimer',
    poster: 'https://images.unsplash.com/photo-1594908900066-3f47337549d8?w=400',
    backdrop: 'https://images.unsplash.com/photo-1594908900066-3f47337549d8?w=1200',
    genre: ['Biography', 'Drama', 'History'],
    duration: 180,
    releaseDate: '2024-01-15',
    rating: 8.5,
    description: 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.',
    director: 'Christopher Nolan',
    cast: ['Cillian Murphy', 'Emily Blunt', 'Matt Damon', 'Robert Downey Jr.'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'now-showing',
    ageRating: 'R',
  },
  {
    id: '2',
    title: 'Barbie',
    originalTitle: 'Barbie',
    poster: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400',
    backdrop: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200',
    genre: ['Adventure', 'Comedy', 'Fantasy'],
    duration: 114,
    releaseDate: '2024-01-20',
    rating: 7.8,
    description: 'Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land.',
    director: 'Greta Gerwig',
    cast: ['Margot Robbie', 'Ryan Gosling', 'America Ferrera', 'Kate McKinnon'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'now-showing',
    ageRating: 'PG-13',
  },
  {
    id: '3',
    title: 'Dune: Part Two',
    originalTitle: 'Dune: Part Two',
    poster: 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=400',
    backdrop: 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=1200',
    genre: ['Action', 'Adventure', 'Sci-Fi'],
    duration: 166,
    releaseDate: '2024-02-01',
    rating: 8.9,
    description: 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.',
    director: 'Denis Villeneuve',
    cast: ['Timothée Chalamet', 'Zendaya', 'Rebecca Ferguson', 'Austin Butler'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'now-showing',
    ageRating: 'PG-13',
  },
  {
    id: '4',
    title: 'The Batman',
    originalTitle: 'The Batman',
    poster: 'https://images.unsplash.com/photo-1509347528160-9a9e33742cdb?w=400',
    backdrop: 'https://images.unsplash.com/photo-1509347528160-9a9e33742cdb?w=1200',
    genre: ['Action', 'Crime', 'Thriller'],
    duration: 176,
    releaseDate: '2024-01-10',
    rating: 8.2,
    description: 'When the Riddler, a sadistic serial killer, begins murdering key political figures in Gotham, Batman is forced to investigate.',
    director: 'Matt Reeves',
    cast: ['Robert Pattinson', 'Zoë Kravitz', 'Paul Dano', 'Colin Farrell'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'now-showing',
    ageRating: 'PG-13',
  },
  {
    id: '5',
    title: 'Killers of the Flower Moon',
    originalTitle: 'Killers of the Flower Moon',
    poster: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400',
    backdrop: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=1200',
    genre: ['Crime', 'Drama', 'History'],
    duration: 206,
    releaseDate: '2024-02-15',
    rating: 8.0,
    description: 'Members of the Osage tribe in the United States are murdered under mysterious circumstances in the 1920s.',
    director: 'Martin Scorsese',
    cast: ['Leonardo DiCaprio', 'Robert De Niro', 'Lily Gladstone', 'Jesse Plemons'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'coming-soon',
    ageRating: 'R',
  },
  {
    id: '6',
    title: 'Guardians of the Galaxy Vol. 3',
    originalTitle: 'Guardians of the Galaxy Vol. 3',
    poster: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=400',
    backdrop: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=1200',
    genre: ['Action', 'Adventure', 'Comedy', 'Sci-Fi'],
    duration: 150,
    releaseDate: '2024-02-20',
    rating: 7.9,
    description: 'Still reeling from the loss of Gamora, Peter Quill rallies his team to defend the universe and one of their own.',
    director: 'James Gunn',
    cast: ['Chris Pratt', 'Zoe Saldana', 'Dave Bautista', 'Karen Gillan'],
    trailer: 'https://youtube.com/watch?v=example',
    status: 'coming-soon',
    ageRating: 'PG-13',
  },
];

const createSeats = (rows: number, columns: number): Seat[] => {
  const seats: Seat[] = [];
  const rowLabels = 'ABCDEFGHIJ';
  
  for (let i = 0; i < rows; i++) {
    for (let j = 1; j <= columns; j++) {
      const isVIP = i >= rows - 2;
      const isCouple = j === 1 || j === columns;
      
      seats.push({
        id: `${rowLabels[i]}${j}`,
        row: rowLabels[i],
        number: j,
        type: isVIP ? 'vip' : isCouple ? 'couple' : 'standard',
        status: Math.random() > 0.7 ? 'sold' : 'available',
        price: isVIP ? 150000 : isCouple ? 180000 : 100000,
      });
    }
  }
  
  return seats;
};

export const mockCinemas: Cinema[] = [
  {
    id: '1',
    name: 'CGV Vincom Center',
    address: '72 Lê Thánh Tôn, Quận 1',
    city: 'Hồ Chí Minh',
    phone: '1900 6017',
    rooms: [
      {
        id: 'r1',
        name: 'Phòng 1',
        cinemaId: '1',
        totalSeats: 100,
        rows: 10,
        columns: 10,
        seats: createSeats(10, 10),
      },
      {
        id: 'r2',
        name: 'Phòng 2',
        cinemaId: '1',
        totalSeats: 80,
        rows: 8,
        columns: 10,
        seats: createSeats(8, 10),
      },
      {
        id: 'r3',
        name: 'Phòng VIP',
        cinemaId: '1',
        totalSeats: 60,
        rows: 6,
        columns: 10,
        seats: createSeats(6, 10),
      },
    ],
  },
  {
    id: '2',
    name: 'Lotte Cinema Landmark 81',
    address: '720A Điện Biên Phủ, Bình Thạnh',
    city: 'Hồ Chí Minh',
    phone: '1900 6017',
    rooms: [
      {
        id: 'r4',
        name: 'Phòng Premium 1',
        cinemaId: '2',
        totalSeats: 60,
        rows: 6,
        columns: 10,
        seats: createSeats(6, 10),
      },
      {
        id: 'r5',
        name: 'Phòng Premium 2',
        cinemaId: '2',
        totalSeats: 60,
        rows: 6,
        columns: 10,
        seats: createSeats(6, 10),
      },
      {
        id: 'r6',
        name: 'Phòng IMAX',
        cinemaId: '2',
        totalSeats: 150,
        rows: 12,
        columns: 12,
        seats: createSeats(12, 12),
      },
    ],
  },
  {
    id: '3',
    name: 'Galaxy Nguyễn Du',
    address: '116 Nguyễn Du, Quận 1',
    city: 'Hồ Chí Minh',
    phone: '1900 2224',
    rooms: [
      {
        id: 'r7',
        name: 'Phòng 1',
        cinemaId: '3',
        totalSeats: 120,
        rows: 10,
        columns: 12,
        seats: createSeats(10, 12),
      },
      {
        id: 'r8',
        name: 'Phòng 2',
        cinemaId: '3',
        totalSeats: 100,
        rows: 10,
        columns: 10,
        seats: createSeats(10, 10),
      },
    ],
  },
  {
    id: '4',
    name: 'BHD Star Cineplex',
    address: '3/2 Street, Quận 10',
    city: 'Hồ Chí Minh',
    phone: '1900 2099',
    rooms: [
      {
        id: 'r9',
        name: 'Screen 1',
        cinemaId: '4',
        totalSeats: 90,
        rows: 9,
        columns: 10,
        seats: createSeats(9, 10),
      },
      {
        id: 'r10',
        name: 'Screen 2',
        cinemaId: '4',
        totalSeats: 110,
        rows: 10,
        columns: 11,
        seats: createSeats(10, 11),
      },
      {
        id: 'r11',
        name: 'Gold Class',
        cinemaId: '4',
        totalSeats: 40,
        rows: 5,
        columns: 8,
        seats: createSeats(5, 8),
      },
    ],
  },
  {
    id: '5',
    name: 'Mega GS Cao Thắng',
    address: '19 Cao Thắng, Quận 3',
    city: 'Hồ Chí Minh',
    phone: '028 3932 2225',
    rooms: [
      {
        id: 'r12',
        name: 'Rạp 1',
        cinemaId: '5',
        totalSeats: 140,
        rows: 12,
        columns: 12,
        seats: createSeats(12, 12),
      },
      {
        id: 'r13',
        name: 'Rạp 2',
        cinemaId: '5',
        totalSeats: 100,
        rows: 10,
        columns: 10,
        seats: createSeats(10, 10),
      },
    ],
  },
  {
    id: '6',
    name: 'CGV Aeon Tân Phú',
    address: '30 Bờ Bao Tân Thắng, Tân Phú',
    city: 'Hồ Chí Minh',
    phone: '1900 6017',
    rooms: [
      {
        id: 'r14',
        name: 'Phòng Standard 1',
        cinemaId: '6',
        totalSeats: 120,
        rows: 10,
        columns: 12,
        seats: createSeats(10, 12),
      },
      {
        id: 'r15',
        name: 'Phòng Standard 2',
        cinemaId: '6',
        totalSeats: 100,
        rows: 10,
        columns: 10,
        seats: createSeats(10, 10),
      },
      {
        id: 'r16',
        name: '4DX',
        cinemaId: '6',
        totalSeats: 80,
        rows: 8,
        columns: 10,
        seats: createSeats(8, 10),
      },
    ],
  },
];

export const mockShowtimes: Showtime[] = [
  // Oppenheimer - Cinema 1
  { id: 's1', movieId: '1', cinemaId: '1', roomId: 'r1', date: '2024-01-20', time: '10:00', price: 100000, availableSeats: 45 },
  { id: 's2', movieId: '1', cinemaId: '1', roomId: 'r1', date: '2024-01-20', time: '13:30', price: 100000, availableSeats: 32 },
  { id: 's3', movieId: '1', cinemaId: '1', roomId: 'r2', date: '2024-01-20', time: '16:00', price: 100000, availableSeats: 28 },
  { id: 's4', movieId: '1', cinemaId: '1', roomId: 'r3', date: '2024-01-20', time: '20:00', price: 150000, availableSeats: 35 },
  { id: 's5', movieId: '1', cinemaId: '1', roomId: 'r1', date: '2024-01-21', time: '11:00', price: 100000, availableSeats: 60 },
  { id: 's6', movieId: '1', cinemaId: '1', roomId: 'r2', date: '2024-01-21', time: '14:30', price: 100000, availableSeats: 50 },
  { id: 's7', movieId: '1', cinemaId: '1', roomId: 'r3', date: '2024-01-21', time: '19:00', price: 150000, availableSeats: 40 },
  
  // Oppenheimer - Cinema 2
  { id: 's8', movieId: '1', cinemaId: '2', roomId: 'r4', date: '2024-01-20', time: '09:30', price: 150000, availableSeats: 25 },
  { id: 's9', movieId: '1', cinemaId: '2', roomId: 'r5', date: '2024-01-20', time: '12:00', price: 150000, availableSeats: 30 },
  { id: 's10', movieId: '1', cinemaId: '2', roomId: 'r6', date: '2024-01-20', time: '18:30', price: 200000, availableSeats: 80 },
  { id: 's11', movieId: '1', cinemaId: '2', roomId: 'r4', date: '2024-01-21', time: '10:00', price: 150000, availableSeats: 35 },
  { id: 's12', movieId: '1', cinemaId: '2', roomId: 'r6', date: '2024-01-21', time: '21:00', price: 200000, availableSeats: 100 },
  
  // Oppenheimer - Cinema 3
  { id: 's13', movieId: '1', cinemaId: '3', roomId: 'r7', date: '2024-01-20', time: '15:00', price: 120000, availableSeats: 55 },
  { id: 's14', movieId: '1', cinemaId: '3', roomId: 'r8', date: '2024-01-20', time: '20:30', price: 120000, availableSeats: 45 },
  { id: 's15', movieId: '1', cinemaId: '3', roomId: 'r7', date: '2024-01-21', time: '16:30', price: 120000, availableSeats: 70 },
  
  // Barbie - Cinema 1
  { id: 's16', movieId: '2', cinemaId: '1', roomId: 'r1', date: '2024-01-20', time: '11:00', price: 100000, availableSeats: 50 },
  { id: 's17', movieId: '2', cinemaId: '1', roomId: 'r2', date: '2024-01-20', time: '14:00', price: 100000, availableSeats: 42 },
  { id: 's18', movieId: '2', cinemaId: '1', roomId: 'r3', date: '2024-01-20', time: '17:30', price: 150000, availableSeats: 38 },
  { id: 's19', movieId: '2', cinemaId: '1', roomId: 'r1', date: '2024-01-21', time: '12:00', price: 100000, availableSeats: 65 },
  
  // Barbie - Cinema 2
  { id: 's20', movieId: '2', cinemaId: '2', roomId: 'r4', date: '2024-01-20', time: '14:00', price: 150000, availableSeats: 22 },
  { id: 's21', movieId: '2', cinemaId: '2', roomId: 'r5', date: '2024-01-20', time: '16:30', price: 150000, availableSeats: 28 },
  { id: 's22', movieId: '2', cinemaId: '2', roomId: 'r6', date: '2024-01-21', time: '15:00', price: 200000, availableSeats: 90 },
  
  // Barbie - Cinema 4
  { id: 's23', movieId: '2', cinemaId: '4', roomId: 'r9', date: '2024-01-20', time: '13:00', price: 110000, availableSeats: 48 },
  { id: 's24', movieId: '2', cinemaId: '4', roomId: 'r10', date: '2024-01-20', time: '18:00', price: 110000, availableSeats: 62 },
  { id: 's25', movieId: '2', cinemaId: '4', roomId: 'r11', date: '2024-01-21', time: '19:30', price: 180000, availableSeats: 25 },
  
  // Dune Part Two - Cinema 2
  { id: 's26', movieId: '3', cinemaId: '2', roomId: 'r6', date: '2024-01-20', time: '19:00', price: 200000, availableSeats: 120 },
  { id: 's27', movieId: '3', cinemaId: '2', roomId: 'r6', date: '2024-01-21', time: '18:00', price: 200000, availableSeats: 135 },
  { id: 's28', movieId: '3', cinemaId: '2', roomId: 'r4', date: '2024-01-20', time: '21:00', price: 150000, availableSeats: 30 },
  
  // Dune Part Two - Cinema 3
  { id: 's29', movieId: '3', cinemaId: '3', roomId: 'r7', date: '2024-01-20', time: '19:00', price: 120000, availableSeats: 65 },
  { id: 's30', movieId: '3', cinemaId: '3', roomId: 'r8', date: '2024-01-20', time: '21:30', price: 120000, availableSeats: 55 },
  { id: 's31', movieId: '3', cinemaId: '3', roomId: 'r7', date: '2024-01-21', time: '20:00', price: 120000, availableSeats: 75 },
  
  // Dune Part Two - Cinema 5
  { id: 's32', movieId: '3', cinemaId: '5', roomId: 'r12', date: '2024-01-20', time: '17:00', price: 130000, availableSeats: 88 },
  { id: 's33', movieId: '3', cinemaId: '5', roomId: 'r13', date: '2024-01-20', time: '20:00', price: 130000, availableSeats: 70 },
  { id: 's34', movieId: '3', cinemaId: '5', roomId: 'r12', date: '2024-01-21', time: '19:30', price: 130000, availableSeats: 95 },
  
  // The Batman - Cinema 1
  { id: 's35', movieId: '4', cinemaId: '1', roomId: 'r2', date: '2024-01-20', time: '18:00', price: 100000, availableSeats: 44 },
  { id: 's36', movieId: '4', cinemaId: '1', roomId: 'r3', date: '2024-01-20', time: '21:30', price: 150000, availableSeats: 32 },
  { id: 's37', movieId: '4', cinemaId: '1', roomId: 'r1', date: '2024-01-21', time: '17:00', price: 100000, availableSeats: 58 },
  
  // The Batman - Cinema 4
  { id: 's38', movieId: '4', cinemaId: '4', roomId: 'r10', date: '2024-01-20', time: '20:00', price: 110000, availableSeats: 68 },
  { id: 's39', movieId: '4', cinemaId: '4', roomId: 'r9', date: '2024-01-21', time: '19:00', price: 110000, availableSeats: 52 },
  { id: 's40', movieId: '4', cinemaId: '4', roomId: 'r11', date: '2024-01-21', time: '22:00', price: 180000, availableSeats: 22 },
  
  // The Batman - Cinema 6
  { id: 's41', movieId: '4', cinemaId: '6', roomId: 'r14', date: '2024-01-20', time: '16:30', price: 105000, availableSeats: 72 },
  { id: 's42', movieId: '4', cinemaId: '6', roomId: 'r15', date: '2024-01-20', time: '19:30', price: 105000, availableSeats: 60 },
  { id: 's43', movieId: '4', cinemaId: '6', roomId: 'r16', date: '2024-01-21', time: '21:00', price: 160000, availableSeats: 45 },
];

export const mockBookings: Booking[] = [
  {
    id: 'b1',
    userId: 'u1',
    showtimeId: 's1',
    seats: ['A1', 'A2'],
    totalPrice: 200000,
    status: 'confirmed',
    createdAt: '2024-01-18T10:30:00',
    paymentMethod: 'credit-card',
  },
  {
    id: 'b2',
    userId: 'u1',
    showtimeId: 's5',
    seats: ['B5', 'B6'],
    totalPrice: 200000,
    status: 'confirmed',
    createdAt: '2024-01-17T15:20:00',
    paymentMethod: 'momo',
  },
];

export const mockTickets: Ticket[] = [
  {
    id: 't1',
    bookingId: 'b1',
    movieTitle: 'Oppenheimer',
    cinemaName: 'CGV Vincom Center',
    roomName: 'Phòng 1',
    date: '2024-01-20',
    time: '10:00',
    seats: ['A1', 'A2'],
    totalPrice: 200000,
    qrCode: 'QR123456789',
    status: 'valid',
  },
];

export const mockReviews: Review[] = [
  {
    id: 'rv1',
    movieId: '1',
    userId: 'u1',
    userName: 'Nguyễn Văn A',
    rating: 5,
    comment: 'Phim rất hay, diễn xuất tuyệt vời! Cảnh quay đẹp mắt và kịch bản chặt chẽ.',
    createdAt: '2024-01-18T12:00:00',
  },
  {
    id: 'rv2',
    movieId: '1',
    userId: 'u2',
    userName: 'Trần Thị B',
    rating: 4,
    comment: 'Phim dài nhưng không thấy nhàm. Rất đáng xem!',
    createdAt: '2024-01-17T18:30:00',
  },
  {
    id: 'rv3',
    movieId: '2',
    userId: 'u3',
    userName: 'Lê Văn C',
    rating: 5,
    comment: 'Phim hài hước, màu sắc rực rỡ. Rất phù hợp để xem cùng gia đình.',
    createdAt: '2024-01-19T09:15:00',
  },
];

export const mockDashboardStats: DashboardStats = {
  totalRevenue: 2450000000,
  ticketsSold: 18540,
  totalMovies: 45,
  totalBookings: 12340,
  revenueGrowth: 12.5,
  ticketsGrowth: 8.3,
};