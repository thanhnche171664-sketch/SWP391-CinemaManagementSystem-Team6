export interface Movie {
  id: string;
  title: string;
  originalTitle: string;
  poster: string;
  backdrop: string;
  genre: string[];
  duration: number;
  releaseDate: string;
  rating: number;
  description: string;
  director: string;
  cast: string[];
  trailer: string;
  status: 'now-showing' | 'coming-soon' | 'ended';
  ageRating: string;
}

export interface Cinema {
  id: string;
  name: string;
  address: string;
  city: string;
  phone: string;
  rooms: Room[];
}

export interface Room {
  id: string;
  name: string;
  cinemaId: string;
  totalSeats: number;
  rows: number;
  columns: number;
  seats: Seat[];
}

export interface Seat {
  id: string;
  row: string;
  number: number;
  type: 'standard' | 'vip' | 'couple';
  status: 'available' | 'reserved' | 'sold';
  price: number;
}

export interface Showtime {
  id: string;
  movieId: string;
  cinemaId: string;
  roomId: string;
  date: string;
  time: string;
  price: number;
  availableSeats: number;
}

export interface Booking {
  id: string;
  userId: string;
  showtimeId: string;
  seats: string[];
  totalPrice: number;
  status: 'pending' | 'confirmed' | 'cancelled';
  createdAt: string;
  paymentMethod: string;
  foodItems?: FoodItem[];
}

export interface Ticket {
  id: string;
  bookingId: string;
  movieTitle: string;
  cinemaName: string;
  roomName: string;
  date: string;
  time: string;
  seats: string[];
  totalPrice: number;
  qrCode: string;
  status: 'valid' | 'used' | 'expired';
}

export interface Review {
  id: string;
  movieId: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface FoodItem {
  id: string;
  name: string;
  price: number;
  image: string;
  category: 'popcorn' | 'drink' | 'combo';
  quantity: number;
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: 'user' | 'admin';
}

export interface DashboardStats {
  totalRevenue: number;
  ticketsSold: number;
  totalMovies: number;
  totalBookings: number;
  revenueGrowth: number;
  ticketsGrowth: number;
}
