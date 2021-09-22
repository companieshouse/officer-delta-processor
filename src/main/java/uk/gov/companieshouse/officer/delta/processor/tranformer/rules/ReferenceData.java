package uk.gov.companieshouse.officer.delta.processor.tranformer.rules;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.officer.delta.processor.model.enums.Pre1992Role;

import java.util.EnumSet;

@Component
public class ReferenceData {

    public static boolean isPre1992Role(String officerRole) {

        return containsRoleName(officerRole, Pre1992Role.class);
    }

    private static <E extends Enum<E>> boolean containsRoleName(String roleName, Class<E> roleClass) {

        return EnumSet.allOf(roleClass).stream().anyMatch(t -> t.toString().equals(roleName));
    }
}
