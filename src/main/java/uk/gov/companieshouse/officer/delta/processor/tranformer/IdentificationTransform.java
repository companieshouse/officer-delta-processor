package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentification;

@Component
public class IdentificationTransform implements
        Transformative<DeltaIdentification, Identification> {

    @Override
    public Identification factory() {
        return new Identification();
    }

    @Override
    public Identification transform(DeltaIdentification source, Identification identification) {

        if (source.getEea() != null) {
            identification.setIdentificationType(IdentificationTypeEnum.EEA);
            identification.setLegalAuthority(source.getEea().getLegalAuthority());
            identification.setLegalForm(source.getEea().getLegalForm());
            identification.setPlaceRegistered(source.getEea().getPlaceRegistered());
            identification.setRegistrationNumber(source.getEea().getRegistrationNumber());

        } else if (source.getOtherCorporateBodyOrFirm() != null) {
            identification.setIdentificationType(
                    IdentificationTypeEnum.OTHER_CORPORATE_BODY_OR_FIRM);
            identification.setLegalAuthority(
                    source.getOtherCorporateBodyOrFirm().getLegalAuthority());
            identification.setLegalForm(source.getOtherCorporateBodyOrFirm().getLegalForm());
            identification.setPlaceRegistered(
                    source.getOtherCorporateBodyOrFirm().getPlaceRegistered());
            identification.setRegistrationNumber(
                    source.getOtherCorporateBodyOrFirm().getRegistrationNumber());

        } else if (source.getNonEeaApi() != null) {
            identification.setIdentificationType(IdentificationTypeEnum.NON_EEA);
            identification.setLegalAuthority(source.getNonEeaApi().getLegalAuthority());
            identification.setLegalForm(source.getNonEeaApi().getLegalForm());
            identification.setPlaceRegistered(source.getNonEeaApi().getPlaceRegistered());
            identification.setRegistrationNumber(source.getNonEeaApi().getRegistrationNumber());

        } else if (source.getUkLimitedCompany() != null) {
            identification.setIdentificationType(IdentificationTypeEnum.UK_LIMITED_COMPANY);
            identification.setLegalAuthority(source.getUkLimitedCompany().getLegalAuthority());
            identification.setLegalForm(source.getUkLimitedCompany().getLegalForm());
            identification.setPlaceRegistered(source.getUkLimitedCompany().getPlaceRegistered());
            identification.setRegistrationNumber(
                    source.getUkLimitedCompany().getRegistrationNumber());

        } else if (source.getRegisteredOverseasEntityCorporateManagingOfficer() != null) {
            identification.setIdentificationType(
                    IdentificationTypeEnum.REGISTERED_OVERSEAS_ENTITY_CORPORATE_MANAGING_OFFICER);

            Identification roeIdentification =
                    source.getRegisteredOverseasEntityCorporateManagingOfficer();
            identification.setLegalAuthority(roeIdentification.getLegalAuthority());
            identification.setLegalForm(roeIdentification.getLegalForm());
            identification.setPlaceRegistered(roeIdentification.getPlaceRegistered());
            identification.setRegistrationNumber(roeIdentification.getRegistrationNumber());

        }

        return identification;
    }
}
