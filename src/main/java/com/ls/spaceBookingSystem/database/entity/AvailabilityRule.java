package com.ls.spaceBookingSystem.database.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "availability_rules")
public class AvailabilityRule extends BaseTimeStamp {
    @EmbeddedId
    private AvailabilityRulePrimaryKey id;

    @Column(name = "is_full_day")
    private boolean isFullDay = false;

    @Convert(converter = SlotsJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slots", nullable = false, columnDefinition = "JSONB")
    private List<TimeSlotRange> slots = new ArrayList<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("templateId")
//    @JoinColumn(name = "template_id")
//    private AvailabilityTemplate template;

    @Converter
    public static class SlotsJsonConverter implements AttributeConverter<List<TimeSlotRange>, String> {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        private static final TypeReference<List<TimeSlotRange>> TYPE = new TypeReference<>() {};
        @Override public String convertToDatabaseColumn(List<TimeSlotRange> a) {
            try { return MAPPER.writeValueAsString(a == null ? List.of() : a); }
            catch (IOException e) {
                throw new AppException(ErrorCode.AVAILABILITY,"Something went wrong. Please try again.")
                        .withDevMessage("FAILED_TO_SERIALIZE_SLOTS, "+e.getMessage());
            }
        }
        @Override public List<TimeSlotRange> convertToEntityAttribute(String d) {
            if (d == null || d.isBlank()) return new ArrayList<>();
            try { return MAPPER.readValue(d, TYPE); }
            catch (IOException e) {
                throw new AppException(ErrorCode.AVAILABILITY,"Something went wrong. Please try again.")
                        .withDevMessage("FAILED_TO_DESERIALIZE_SLOTS, "+e.getMessage());
            }
        }
    }

    public DayOfWeekEnum getDayOfWeek() { return id.getDayOfWeek(); }
}
