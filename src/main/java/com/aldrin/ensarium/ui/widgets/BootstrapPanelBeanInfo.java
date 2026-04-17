/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldrin.ensarium.ui.widgets;

/**
 *
 * @author ALDRIN CABUSOG
 */
import java.beans.*;

public class BootstrapPanelBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(BootstrapPanel.class);

        bd.setDisplayName("BootstrapPanel");
        bd.setShortDescription("Bootstrap-like panel with header and body");

        // IMPORTANT:
        // tells NetBeans GUI Builder to drop child components into bodyPanel
        bd.setValue("containerDelegate", "getBodyPanel");

        return bd;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor title = new PropertyDescriptor("title", BootstrapPanel.class);
            PropertyDescriptor variantName = new PropertyDescriptor("variantName", BootstrapPanel.class);
            PropertyDescriptor radius = new PropertyDescriptor("radius", BootstrapPanel.class);
            PropertyDescriptor headerHeight = new PropertyDescriptor("headerHeight", BootstrapPanel.class);
            PropertyDescriptor hoverEnabled = new PropertyDescriptor("hoverEnabled", BootstrapPanel.class);
            PropertyDescriptor contentLayoutName = new PropertyDescriptor("contentLayoutName", BootstrapPanel.class);

            return new PropertyDescriptor[] {
                title,
                variantName,
                radius,
                headerHeight,
                hoverEnabled,
                contentLayoutName
            };
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}