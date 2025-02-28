package com.wildcat.views.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wildcat.views.pages.AiConfiguration;
import com.wildcat.views.pages.ApiCalls;
import com.wildcat.views.pages.ChatView;

public class AiAssistantLayout extends AppLayout {
    public AiAssistantLayout(){
        DrawerToggle toggle = new DrawerToggle();
        toggle.clickInClient();

        H1 wildcatAiAssistant = new H1("WildCat | AI Assistant");
        wildcatAiAssistant.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        SideNav nav = getSideNav();

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, wildcatAiAssistant);
    }

    private SideNav getSideNav() {
        SideNav sideNav = new SideNav();

        H1 aiAssistantTitle = new H1("AI Assistant");
        aiAssistantTitle.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");


        SideNavItem aiConfigurationLink = new SideNavItem("Configuration", AiConfiguration.class, VaadinIcon.CONTROLLER.create());
        SideNavItem openAiChatLink = new SideNavItem("OpenAI Chat", ChatView.class, VaadinIcon.CHAT.create());
        SideNavItem apiCallsLink = new SideNavItem("Api Calls", ApiCalls.class, VaadinIcon.CHAT.create());
        sideNav.addItem(aiConfigurationLink);
        sideNav.addItem(openAiChatLink);
        sideNav.addItem(apiCallsLink);

        return sideNav;
    }
}
