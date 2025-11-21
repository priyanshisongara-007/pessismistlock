import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingTest {

    public static void main(String[] args) throws InterruptedException {
        BookingService bookingService = new BookingService();

        System.out.println("Starting bookings with FOR UPDATE locking...");
        runConcurrentBooking(bookingService, BookingType.FOR_UPDATE);

       System.out.println("\nStarting bookings with FOR UPDATE SKIP LOCKED...");
        runConcurrentBooking(bookingService, BookingType.SKIP_LOCKED);

        System.out.println("\nStarting bookings without locking...");
        runConcurrentBooking(bookingService, BookingType.NO_LOCK);
    }

    enum BookingType { FOR_UPDATE, SKIP_LOCKED, NO_LOCK }

    private static void runConcurrentBooking(BookingService service, BookingType type) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int userId = 1; userId <= 120; userId++) {
            int uid = userId;
            executor.submit(() -> {
                switch (type) {
                    case FOR_UPDATE -> service.bookSeatForUpdate(uid);
                    case SKIP_LOCKED -> service.bookSeatForUpdateSkipLocked(uid);
                    case NO_LOCK -> service.bookSeatNoLock(uid);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }
    }
}
