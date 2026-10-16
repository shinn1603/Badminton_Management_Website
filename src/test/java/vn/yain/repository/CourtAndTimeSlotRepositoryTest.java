package vn.yain.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.yain.entity.Court;
import vn.yain.entity.TimeSlot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourtAndTimeSlotRepositoryTest {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Test
    @DisplayName("Kiểm tra lưu và truy vấn TimeSlot")
    void testSaveAndTimeSlotQueries() {
        TimeSlot slot = new TimeSlot();
        slot.setSlotCode("KG01");
        slot.setStartTime("06:00");
        slot.setEndTime("09:00");
        slot.setSlotType("Thường (Sáng sớm)");

        timeSlotRepository.save(slot);

        Optional<TimeSlot> found = timeSlotRepository.findById("KG01");
        assertTrue(found.isPresent());
        assertEquals("06:00", found.get().getStartTime());
        assertEquals("09:00", found.get().getEndTime());
        assertEquals("Thường (Sáng sớm)", found.get().getSlotType());
    }

    @Test
    @DisplayName("Kiểm tra lưu và truy vấn Court theo chi nhánh và mã sân")
    void testSaveAndCourtQueries() {
        Court court = new Court();
        court.setCourtCode("CL01");
        court.setBranchCode("CN01");
        court.setCourtName("Sân cầu lông 1 (VIP Yonex)");
        court.setCourtType("Cầu lông VIP");
        court.setHourlyRate(new BigDecimal("120000.00"));
        court.setStatus("Trống");
        court.setImageUrl("/images/badminton-court.jpg");

        Court saved = courtRepository.save(court);
        assertNotNull(saved.getId());

        Optional<Court> byCode = courtRepository.findByCourtCode("CL01");
        assertTrue(byCode.isPresent());
        assertEquals("CN01", byCode.get().getBranchCode());
        assertEquals(new BigDecimal("120000.00"), byCode.get().getHourlyRate());

        List<Court> byBranch = courtRepository.findByBranchCode("CN01");
        assertFalse(byBranch.isEmpty());
        assertEquals(1, byBranch.size());
        assertEquals("CL01", byBranch.get(0).getCourtCode());

        List<Court> nonExistentBranch = courtRepository.findByBranchCode("CN99");
        assertTrue(nonExistentBranch.isEmpty());
    }
}
