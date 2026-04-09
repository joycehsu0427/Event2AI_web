package event.to.ai.backend.domainmodel.adapter.out.persistence.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = false)
public class DomainAttributeListConverter implements AttributeConverter<List<DomainAttributeData>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<DomainAttributeData>> ATTRIBUTE_LIST_TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<DomainAttributeData> attributeData) {
        List<DomainAttributeData> safeValue = attributeData == null ? new ArrayList<>() : attributeData;
        try {
            return OBJECT_MAPPER.writeValueAsString(safeValue);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize domain attributes to JSON", e);
        }
    }

    @Override
    public List<DomainAttributeData> convertToEntityAttribute(String databaseData) {
        if (databaseData == null || databaseData.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return OBJECT_MAPPER.readValue(databaseData, ATTRIBUTE_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize domain attributes from JSON", e);
        }
    }
}
