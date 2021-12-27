package dev.geco.gsit.manager;

import java.util.*;
import java.util.regex.*;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;

import dev.geco.gsit.GSitMain;

public class MManager {

    private final GSitMain GPM;

    public MManager(GSitMain GPluginMain) { GPM = GPluginMain; }

    private final char C = '&';

    private final boolean A = Arrays.stream(ChatColor.class.getMethods()).filter(m -> "of".equals(m.getName())).findFirst().orElse(null) != null;

    private final String P = "(<click:.+:.+>.+</click>)|(<item:\\d+>.+</item>)|(<text:.+>.+</text>)|(<translate:\\d+>)|(<translatekey:.+>)|(&[\\da-fklmnor])";

    private final String PA = P + "|(#[0-9a-fA-F]{6})";

    private final boolean R = true;

    public String toColoredString(String Text) {
        String r = ChatColor.translateAlternateColorCodes(C, Text);
        if(A) {
            Matcher m = Pattern.compile("(#[0-9a-fA-F]{6})").matcher(r);
            while(m.find()) r = r.replace(m.group(), ChatColor.of(m.group()).toString());
        }
        return r;
    }

    public void sendMessage(CommandSender S, String Message, Object... ReplaceList) { S.sendMessage(getMessage(Message, ReplaceList)); }

    public void sendMessage(CommandSender S, BaseComponent Message) { S.spigot().sendMessage(Message); }

    public void sendMessage(CommandSender S, BaseComponent[] Message) { S.spigot().sendMessage(Message); }

    public String getMessage(String Message, Object... ReplaceList) { return toColoredString(getRawMessage(Message, ReplaceList)); }

    public String getRawMessage(String Message, Object... ReplaceList) {
        String m = Message == null || Message.equals("") ? "" : GPM.getMessages().getString(Message, Message);
        return replace(m, ReplaceList);
    }

    public List<String> getMessageList(String Message, Object... ReplaceList) {
        List<String> l = new ArrayList<>();
        for(String i : GPM.getMessages().getStringList(Message)) l.add(toColoredString(replace(i, ReplaceList)));
        return l;
    }

    public List<String> getRawMessageList(String Message, Object... ReplaceList) {
        List<String> l = new ArrayList<>();
        for(String i : GPM.getMessages().getStringList(Message)) l.add(replace(i, ReplaceList));
        return l;
    }

    public String replace(String Message, Object... ReplaceList) {
        String m = Message;
        if(ReplaceList.length > 1) for(int i = 0; i < ReplaceList.length; i += 2) if(ReplaceList[i] != null && ReplaceList[i + 1] != null) m = m.replace(ReplaceList[i].toString(), ReplaceList[i + 1].toString());
        return m.replace("[P]", GPM.getPrefix());
    }

    public BaseComponent getComplexMessage(String Message, ItemStack... ReplaceList) {
        ComponentBuilder cb = new ComponentBuilder();

        Matcher m = Pattern.compile(A ? PA : P).matcher(Message);

        List<ComplexComponent> eos = new ArrayList<>();

        while(m.find()) {
            if(m.group().contains(":")) {
                String mg = m.group().substring(1, m.group().indexOf(">"));
                String[] s = mg.split(":");
                BaseComponent bc = null;
                switch(s[0]) {
                    case "click":
                        bc = getComplexMessage(m.group().substring(m.group().indexOf(">") + 1, m.group().lastIndexOf("<")));
                        try {
                            bc.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(s[1].toUpperCase()), s[2]));
                        } catch(IllegalArgumentException e) { }
                        break;
                    case "item":
                        bc = getComplexMessage(m.group().substring(m.group().indexOf(">") + 1, m.group().lastIndexOf("<")));
                        try {
                            int i = Integer.parseInt(s[1]);
                            if(ReplaceList.length - 1 >= i) {
                                ItemStack is = ReplaceList[i];
                                try {
                                    Class<?> cis = NMSManager.getOBCClass("inventory.CraftItemStack");
                                    Object nis = NMSManager.getMethod("asNMSCopy", cis, ItemStack.class).invoke(null, is);
                                    Object nbt = NMSManager.getMethod("getOrCreateTag", nis.getClass()).invoke(nis);
                                    bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new net.md_5.bungee.api.chat.hover.content.Item(is.getType().getKey().toString(), is.getAmount(), ItemTag.ofNbt(nbt.toString()))));
                                } catch(Exception e) { e.printStackTrace(); }
                            }
                        } catch(NumberFormatException e) { }
                        break;
                    case "text":
                        bc = new TextComponent(m.group().substring(m.group().indexOf(">") + 1, m.group().lastIndexOf("<")));
                        bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(s[1])));
                        break;
                    case "translate":
                        try {
                            int i = Integer.parseInt(s[1]);
                            if(ReplaceList.length - 1 >= i) {
                                ItemStack is = ReplaceList[i];
                                bc = new TranslatableComponent((is.getType().isBlock() ? "block" : "item") + ".minecraft." + is.getType().getKey().getKey());
                            }
                        } catch(NumberFormatException e) { }
                        break;
                    case "translatekey":
                        bc = new TranslatableComponent(s[1]);
                        break;
                }
                eos.add(new ComplexComponent(bc, null));
            } else {
                eos.add(new ComplexComponent(null, m.group()));
            }
        }

        int i = 0;

        List<String> z = Arrays.asList(Message.split(A ? PA : P));

        for(String s : z) {
            cb.append(s, FormatRetention.FORMATTING);
            if(i > -1 && i < eos.size()) {
                ComplexComponent eob = eos.get(i);
                if(eob.getColor() != null) {
                    cb.append("", FormatRetention.FORMATTING);
                    if(eob.getColor().startsWith("&")) {
                        String c = eob.getColor().replace("&", "");
                        switch(c) {
                            case "a":
                            case "b":
                            case "c":
                            case "d":
                            case "e":
                            case "f":
                            case "0":
                            case "1":
                            case "2":
                            case "3":
                            case "4":
                            case "5":
                            case "6":
                            case "7":
                            case "8":
                            case "9":
                                if(R) cb.reset();
                                cb.color(ChatColor.getByChar(c.charAt(0)));
                                break;
                            case "k":
                                cb.obfuscated(true);
                            case "l":
                                cb.bold(true);
                                break;
                            case "m":
                                cb.strikethrough(true);
                                break;
                            case "n":
                                cb.underlined(true);
                                break;
                            case "o":
                                cb.italic(true);
                                break;
                            case "r":
                                cb.reset();
                                break;
                        }
                    } else {
                        cb.color(ChatColor.of(eob.getColor()));
                    }
                } else {
                    if(eob.getBase() != null) cb.append(eob.getBase());
                }
            }
            i++;
        }

        return z.size() == 0 && eos.size() > 0 && eos.get(0).getBase() != null ? eos.get(0).getBase() : new TextComponent(cb.create());
    }

    private static class ComplexComponent {

        private final BaseComponent b;

        private final String c;

        public ComplexComponent(BaseComponent b, String c) {
            this.b = b;
            this.c = c;
        }

        public BaseComponent getBase() { return b; }

        public String getColor() { return c; }

    }

}