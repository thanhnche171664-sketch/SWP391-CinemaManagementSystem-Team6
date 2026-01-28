import { useState } from 'react';
import { Layout } from '@/app/components/Layout';
import { AdminLayout } from '@/app/components/AdminLayout';
import { ManagerLayout } from '@/app/components/ManagerLayout';

// User Pages
import { HomePage } from '@/app/pages/HomePage';
import { MovieDetailPage } from '@/app/pages/MovieDetailPage';
import { ShowtimesPage } from '@/app/pages/ShowtimesPage';
import { SeatSelectionPage } from '@/app/pages/SeatSelectionPage';
import { PaymentPage } from '@/app/pages/PaymentPage';
import { TicketPage } from '@/app/pages/TicketPage';
import { BookingHistoryPage } from '@/app/pages/BookingHistoryPage';
import { ReviewPage } from '@/app/pages/ReviewPage';

// Admin Pages
import { DashboardPage } from '@/app/pages/admin/DashboardPage';
import { MoviesManagementPage } from '@/app/pages/admin/MoviesManagementPage';
import { CinemasManagementPage } from '@/app/pages/admin/CinemasManagementPage';
import { ShowtimesManagementPage } from '@/app/pages/admin/ShowtimesManagementPage';
import { BookingsManagementPage } from '@/app/pages/admin/BookingsManagementPage';
import { ReportsPage } from '@/app/pages/admin/ReportsPage';

// Manager Pages
import { ManagerDashboardPage } from '@/app/pages/manager/ManagerDashboardPage';

type UserPage =
  | 'home'
  | 'movies'
  | 'bookings'
  | 'movie-detail'
  | 'seat-selection'
  | 'payment'
  | 'ticket'
  | 'review';

type AdminPage = 'dashboard' | 'movies' | 'cinemas' | 'showtimes' | 'bookings' | 'reports';

type ManagerPage = 'dashboard' | 'showtimes' | 'bookings' | 'reports';

type AppMode = 'user' | 'admin' | 'manager';

interface AppState {
  mode: AppMode;
  userPage: UserPage;
  adminPage: AdminPage;
  managerPage: ManagerPage;
  selectedMovieId?: string;
  selectedShowtimeId?: string;
  selectedSeats?: string[];
  totalPrice?: number;
  bookingId?: string;
}

export default function App() {
  const [state, setState] = useState<AppState>({
    mode: 'user',
    userPage: 'home',
    adminPage: 'dashboard',
    managerPage: 'dashboard',
  });

  const navigateUser = (page: UserPage) => {
    setState((prev) => ({ ...prev, userPage: page }));
  };

  const navigateAdmin = (page: AdminPage) => {
    setState((prev) => ({ ...prev, adminPage: page }));
  };

  const navigateManager = (page: ManagerPage) => {
    setState((prev) => ({ ...prev, managerPage: page }));
  };

  const handleMovieClick = (movieId: string) => {
    setState((prev) => ({ ...prev, selectedMovieId: movieId, userPage: 'movie-detail' }));
  };

  const handleBooking = (movieId: string) => {
    setState((prev) => ({ ...prev, selectedMovieId: movieId, userPage: 'movie-detail' }));
  };

  const handleSelectShowtime = (showtimeId: string) => {
    setState((prev) => ({ ...prev, selectedShowtimeId: showtimeId, userPage: 'seat-selection' }));
  };

  const handleSeatConfirm = (seats: string[], totalPrice: number) => {
    setState((prev) => ({
      ...prev,
      selectedSeats: seats,
      totalPrice,
      userPage: 'payment',
    }));
  };

  const handlePaymentConfirm = (bookingId: string) => {
    setState((prev) => ({ ...prev, bookingId, userPage: 'ticket' }));
  };

  const handleReview = (movieId: string) => {
    setState((prev) => ({ ...prev, selectedMovieId: movieId, userPage: 'review' }));
  };

  const renderUserPage = () => {
    switch (state.userPage) {
      case 'home':
        return <HomePage onMovieClick={handleMovieClick} />;

      case 'movie-detail':
        return (
          <MovieDetailPage
            movieId={state.selectedMovieId!}
            onBack={() => navigateUser('home')}
            onBooking={handleBooking}
            onReview={handleReview}
            onSelectShowtime={handleSelectShowtime}
          />
        );

      case 'seat-selection':
        return (
          <SeatSelectionPage
            showtimeId={state.selectedShowtimeId!}
            onBack={() => setState((prev) => ({ ...prev, userPage: 'movie-detail' }))}
            onConfirm={handleSeatConfirm}
          />
        );

      case 'payment':
        return (
          <PaymentPage
            showtimeId={state.selectedShowtimeId!}
            selectedSeats={state.selectedSeats!}
            totalPrice={state.totalPrice!}
            onBack={() => navigateUser('seat-selection')}
            onConfirm={handlePaymentConfirm}
          />
        );

      case 'ticket':
        return <TicketPage bookingId={state.bookingId!} onBackHome={() => navigateUser('home')} />;

      case 'bookings':
        return (
          <BookingHistoryPage
            onViewTicket={(bookingId) =>
              setState((prev) => ({ ...prev, bookingId, userPage: 'ticket' }))
            }
          />
        );

      case 'review':
        return (
          <ReviewPage
            movieId={state.selectedMovieId!}
            onBack={() =>
              setState((prev) => ({ ...prev, userPage: 'movie-detail' }))
            }
            onSubmit={() =>
              setState((prev) => ({ ...prev, userPage: 'movie-detail' }))
            }
          />
        );

      default:
        return <HomePage onMovieClick={handleMovieClick} />;
    }
  };

  const renderAdminPage = () => {
    switch (state.adminPage) {
      case 'dashboard':
        return <DashboardPage />;
      case 'movies':
        return <MoviesManagementPage />;
      case 'cinemas':
        return <CinemasManagementPage />;
      case 'showtimes':
        return <ShowtimesManagementPage />;
      case 'bookings':
        return <BookingsManagementPage />;
      case 'reports':
        return <ReportsPage />;
      default:
        return <DashboardPage />;
    }
  };

  const renderManagerPage = () => {
    switch (state.managerPage) {
      case 'dashboard':
        return <ManagerDashboardPage />;
      case 'showtimes':
        return <ShowtimesManagementPage />;
      case 'bookings':
        return <BookingsManagementPage />;
      case 'reports':
        return <ReportsPage />;
      default:
        return <ManagerDashboardPage />;
    }
  };

  if (state.mode === 'admin') {
    return (
      <div className="dark">
        <AdminLayout
          currentPage={state.adminPage}
          onNavigate={(page) => navigateAdmin(page as AdminPage)}
          onBackToUser={() => setState((prev) => ({ ...prev, mode: 'user' }))}
        >
          {renderAdminPage()}
        </AdminLayout>
      </div>
    );
  }

  if (state.mode === 'manager') {
    return (
      <div className="dark">
        <ManagerLayout
          currentPage={state.managerPage}
          onNavigate={(page) => navigateManager(page as ManagerPage)}
          onBackToUser={() => setState((prev) => ({ ...prev, mode: 'user' }))}
        >
          {renderManagerPage()}
        </ManagerLayout>
      </div>
    );
  }

  return (
    <div className="dark">
      <Layout
        currentPage={state.userPage}
        onNavigate={(page) => {
          if (page === 'admin') {
            setState((prev) => ({ ...prev, mode: 'admin' }));
          } else if (page === 'manager') {
            setState((prev) => ({ ...prev, mode: 'manager' }));
          } else {
            navigateUser(page as UserPage);
          }
        }}
      >
        {renderUserPage()}
      </Layout>
    </div>
  );
}