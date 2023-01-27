package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;

@Component
public class IdentificationTransform implements Transformative<Identification, uk.gov.companieshouse.api.appointment.Identification> {
    @Override
    public uk.gov.companieshouse.api.appointment.Identification factory() {
        return new uk.gov.companieshouse.api.appointment.Identification();
    }

    @Override
    public uk.gov.companieshouse.api.appointment.Identification transform(Identification source, uk.gov.companieshouse.api.appointment.Identification identification) {

        if (source.getEea() != null) {

            identification.setIdentificationType(uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.fromValue("eea"));
            identification.setLegalAuthority(source.getEea().getLegalAuthority());
            identification.setLegalForm(source.getEea().getLegalForm());
            identification.setPlaceRegistered(source.getEea().getPlaceRegistered());
            identification.setRegistrationNumber(source.getEea().getRegistrationNumber());

        } else if (source.getOtherCorporateBodyOrFirm() != null) {

            identification.setIdentificationType(uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.fromValue("other-corporate-body-or-firm"));
            identification.setLegalAuthority(source.getOtherCorporateBodyOrFirm().getLegalAuthority());
            identification.setLegalForm(source.getOtherCorporateBodyOrFirm().getLegalForm());
            identification.setPlaceRegistered(source.getOtherCorporateBodyOrFirm().getPlaceRegistered());
            identification.setRegistrationNumber(source.getOtherCorporateBodyOrFirm().getRegistrationNumber());

        } else if (source.getNonEeaApi() != null) {

            identification.setIdentificationType(uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.fromValue("non-eea"));
            identification.setLegalAuthority(source.getNonEeaApi().getLegalAuthority());
            identification.setLegalForm(source.getNonEeaApi().getLegalForm());
            identification.setPlaceRegistered(source.getNonEeaApi().getPlaceRegistered());
            identification.setRegistrationNumber(source.getNonEeaApi().getRegistrationNumber());

        } else if (source.getUKLimitedCompany() != null) {

            identification.setIdentificationType(uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.fromValue("uk-limited"));
            identification.setLegalAuthority(source.getUKLimitedCompany().getLegalAuthority());
            identification.setLegalForm(source.getUKLimitedCompany().getLegalForm());
            identification.setPlaceRegistered(source.getUKLimitedCompany().getPlaceRegistered());
            identification.setRegistrationNumber(source.getUKLimitedCompany().getRegistrationNumber());

        }

        return identification;
    }
}
