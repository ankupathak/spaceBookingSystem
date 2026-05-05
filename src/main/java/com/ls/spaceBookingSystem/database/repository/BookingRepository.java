package com.ls.spaceBookingSystem.database.repository;

import com.ls.spaceBookingSystem.database.entity.Booking;
import com.ls.spaceBookingSystem.common.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    @Query(value = """
        SELECT COUNT(*) > 0 FROM bookings
        WHERE space_id = :spaceId
          AND status   = 'CONFIRMED'
          AND start    < :end
          AND end      > :start
        """,
            nativeQuery = true
    )
    long existsConfirmedOverlap(
            @Param("spaceId") Long    spaceId,
            @Param("start")   Instant start,
            @Param("end")     Instant end
    );

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")) // 0 = NO WAIT
    @Query("""
        SELECT b FROM Booking b
        WHERE b.spaceId    = :spaceId
          AND b.status     IN ('PENDING')
          AND b.start < :end
          AND b.end   > :start
          AND b.bookingId  != :excludeId
        """)
    List<Booking> findOverlappingPending(
            @Param("spaceId")     Long spaceId,
            @Param("start")  Instant start,
            @Param("end")    Instant end,
            @Param("excludeId")   Long excludeId
    );


    @Query("SELECT b FROM Booking b WHERE b.bookingId = :id AND b.bookerUserId = :bookerUserId AND (b.status = 'CONFIRMED' OR b.status = 'CANCELLED')")
    Optional<Booking> findConfirmedById(@Param("id") Long id, @Param("bookerUserId") long bookerUserId);


    List<Booking> findByBookerUserIdAndStatusOrderByStartAsc(
            Long bookerUserId, BookingStatus status);


    @Query("""
        SELECT b FROM Booking b
        WHERE b.spaceId    = :spaceId
          AND b.status     = 'CONFIRMED'
          AND b.start < :windowEnd
          AND b.end   > :windowStart
        ORDER BY b.start
        """)
    List<Booking> findConfirmedInRange(
            @Param("spaceId")     Long spaceId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd")   LocalDateTime windowEnd
    );

    @Query("DELETE FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :threshold")
    @org.springframework.data.jpa.repository.Modifying
    int deleteStalePendingBookings(@Param("threshold") LocalDateTime threshold);
}
