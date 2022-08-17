package no.sikt.nva.lms.libs;

public class CommonCartridge {

    public static final String CONTACT_EMAIL = "support@bibsys.no";
    public static final String VENDOR_NAME = "BIBSYS";
    public static final String VENDOR_CODE = "bibsys.no";

    private final CartridgeBasicLTILinkType cbllt;

    private CommonCartridge(CartridgeBasicLTILinkType cbllt) {
        this.cbllt = cbllt;
    }

    public String toXml() {
        try {
            return no.unit.ltilib.cc.CCUtil.commonCartridgeToString(cbllt);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class Builder {

        private final CartridgeBasicLTILinkType cbllt;
        private no.unit.ltilib.cc.Extension extension = null;

        public Builder() {
            cbllt = new CartridgeBasicLTILinkType();
        }

        public Builder setTitle(String title) {
            cbllt.setTitle(title);
            return this;
        }

        public Builder setDescription(String description) {
            cbllt.setDescription(description);
            return this;
        }

        public Builder setLaunchUrl(String url) {
            cbllt.setLaunchUrl(url);
            return this;
        }

        public Builder setIconUrl(String url) {
            IconType icon = new IconType();
            icon.setValue(url);
            cbllt.setIcon(icon);
            return this;
        }

        public Builder setExtension(no.unit.ltilib.cc.Extension extension) {
            this.extension = extension;
            return this;
        }

        public CommonCartridge build() {
            if (extension != null) {

                PlatformPropertySetType ppst = new PlatformPropertySetType();

                ppst.setPlatform(extension.getPlatform());

                Map<String, String> properties = extension.getProperties();
                for (String propName : properties.keySet()) {
                    PropertyType pt = new PropertyType();
                    pt.setName(propName);
                    pt.setValue(properties.get(propName));
                    ppst.getProperty().add(pt);
                }

                if (extension instanceof CanvasExtension) {
                    CanvasExtension canvasExtension = (CanvasExtension) extension;
                    for (CanvasExtension.Option option : canvasExtension.getOptions()) {
                        OptionsType ot = new OptionsType();

                        ot.setName(option.getName());

                        Map<String, String> optionProps = option.getProperties();

                        if (!optionProps.containsKey("url") && cbllt.getLaunchUrl() != null) {
                            PropertyType pt = new PropertyType();
                            pt.setName("url");
                            pt.setValue(cbllt.getLaunchUrl());
                            ot.getProperty().add(pt);
                        }

                        if (!optionProps.containsKey("text")) {
                            PropertyType pt = new PropertyType();
                            pt.setName("text");
                            pt.setValue(cbllt.getTitle());
                            ot.getProperty().add(pt);
                        }

                        for (String propName : optionProps.keySet()) {
                            PropertyType pt = new PropertyType();
                            pt.setName(propName);
                            pt.setValue(optionProps.get(propName));
                            ot.getProperty().add(pt);
                        }

                        if (!optionProps.containsKey("enabled")) {
                            PropertyType pt = new PropertyType();
                            pt.setName("enabled");
                            pt.setValue("true");
                            ot.getProperty().add(pt);
                        }

                        ppst.getOptions().add(ot);
                    }
                }

                cbllt.setExtensions(ppst);
            }

            if (cbllt.getVendor() == null) {
                //Add default vendor
                VendorType vendorType = new VendorType();
                vendorType.setCode(VENDOR_CODE);
                ContactType ct = new ContactType();
                ct.setEmail(CONTACT_EMAIL);
                vendorType.setContact(ct);
                LocalizedStringType name = new LocalizedStringType();
                name.setValue(VENDOR_NAME);
                vendorType.setName(name);
                cbllt.setVendor(vendorType);
            }

            if (cbllt.getCartridgeBundle() == null) {
                ResourceRefType cartridgeBundle = new ResourceRefType();
                cartridgeBundle.setIdentifierref("BLTI001_Bundle");
                cbllt.setCartridgeBundle(cartridgeBundle);
            }
            if (cbllt.getCartridgeIcon() == null) {
                ResourceRefType cartridgeIcon = new ResourceRefType();
                cartridgeIcon.setIdentifierref("BLTI001_Icon");
                cbllt.setCartridgeIcon(cartridgeIcon);
            }
            return new CommonCartridge(cbllt);
        }
    }

    public static void main(String... args) throws Exception {
        String title = "BIBSYS LTI Tool";
        String description = "An LTI App for BIBSYS xyz";
        String launchUrl = "http://myhost.com/launch";
        String iconUrl = "http://myhost.com/icon.png";
        String toolId = "my_lti_tool";

        String xml = new Builder()
                         .setTitle(title)
                         .setDescription(description)
                         .setLaunchUrl(launchUrl)
                         .setIconUrl(iconUrl)
                         .setExtension(new CanvasExtension(toolId, CanvasExtension.PrivacyLevel.PUBLIC)
                                           .addOption(new EditorButton()
                                                          .setIconUrl(iconUrl)
                                                          .setSelectionWidth(400)
                                                          .setSelectionHeight(300)
                                           )
                                           .addOption(new CourseNavigation()
                                                          .setVisibility(CourseNavigation.Visibility.PUBLIC)
                                                          .setDefault(CourseNavigation.Default.ENABLED)
                                           )
                         )
                         .build()
                         .toXml();

        System.out.println(xml);
    }
}

