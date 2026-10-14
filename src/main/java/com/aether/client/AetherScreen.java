package com.aether.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Aether's client-only cosmic navigation room. Movement here changes the virtual camera only. */
public final class AetherScreen extends Screen {
    private String category = "All";
    private ModuleEntry selected;
    private double fade = 0, cameraX = 0, cameraY = 0, yaw = 0, pitch = 0, zoom = 1;
    private int scroll = 0, panelX, panelY;
    private boolean dragging, looking;
    private int dragX, dragY, lastMouseX, lastMouseY;
    private final List<Star> stars = new ArrayList<>();
    private final List<Orb> orbs = new ArrayList<>();

    public AetherScreen() {
        super(Text.literal("Aether Cosmic Dimension"));
        for (int i = 0; i < 420; i++) stars.add(new Star(Math.random() * 2 - 1, Math.random() * 2 - 1, .08 + Math.random() * .92, Math.random() * 6.28));
        for (int i = 0; i < 7; i++) orbs.add(new Orb(Math.random() * 2 - 1, Math.random() * 2 - 1, .3 + Math.random() * .7, 18 + Math.random() * 32));
    }

    @Override protected void init() {
        if (panelX == 0) { panelX = width / 2 - 205; panelY = height / 2 - 125; }
        panelX = Math.max(12, Math.min(panelX, width - 300));
        panelY = Math.max(12, Math.min(panelY, height - 230));
    }

    @Override public void tick() {
        fade = Math.min(1, fade + .045);
        // WASD moves the virtual camera; it never moves the real Minecraft player.
        double speed = 0.025 / zoom;
        if (held(GLFW.GLFW_KEY_W)) cameraY += speed;
        if (held(GLFW.GLFW_KEY_S)) cameraY -= speed;
        if (held(GLFW.GLFW_KEY_A)) cameraX += speed;
        if (held(GLFW.GLFW_KEY_D)) cameraX -= speed;
        if (held(GLFW.GLFW_KEY_SPACE)) pitch = Math.min(1.2, pitch + .012);
        if (held(GLFW.GLFW_KEY_LEFT_CONTROL)) pitch = Math.max(-1.2, pitch - .012);
    }

    private boolean held(int key) {
        long window = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = width, h = height;
        ctx.fill(0, 0, w, h, 0xFF03040D);
        // Layered nebula haze and distant galactic bands.
        for (int i = 0; i < 6; i++) {
            int yy = h / 2 - 90 + i * 27 + (int)(Math.sin(fade * 2 + i) * 8) - (int)(cameraY * 18);
            ctx.fill(0, yy, w, yy + 20, i % 2 == 0 ? 0x123B1D87 : 0x10205D92);
        }
        // Perspective starfield: each star's position shifts according to virtual camera movement.
        for (Star s : stars) {
            double depth = Math.max(.12, s.depth);
            double sx = (s.x / depth * 0.22 - cameraX * (1.0 - depth) * 2.2 + yaw * .3);
            double sy = (s.y / depth * 0.22 + cameraY * (1.0 - depth) * 2.2 + pitch * .25);
            int x = (int)(((sx + 1.5) * .5 * w) % w); if (x < 0) x += w;
            int y = (int)(((sy + 1.5) * .5 * h) % h); if (y < 0) y += h;
            int glow = (int)(130 + 110 * (.5 + .5 * Math.sin(fade * 4 + s.phase)));
            int color = (glow << 24) | 0x00AEEFFF;
            int size = depth > .78 ? 2 : 1;
            ctx.fill(x, y, x + size, y + size, color);
        }
        // Planet silhouettes with glowing atmospheric rims.
        for (int i = 0; i < orbs.size(); i++) {
            Orb o = orbs.get(i);
            int ox = (int)(w * (.5 + o.x * .48) - cameraX * o.depth * 25);
            int oy = (int)(h * (.5 + o.y * .42) + cameraY * o.depth * 25 + Math.sin(fade * 1.5 + i) * 3);
            int r = (int)(o.radius * o.depth);
            ctx.fill(ox-r-2, oy-r-2, ox+r+2, oy+r+2, 0x183B8CFF);
            ctx.fill(ox-r, oy-r, ox+r, oy+r, i % 2 == 0 ? 0xFF101B43 : 0xFF171237);
            ctx.fill(ox-r, oy-r, ox+r, oy-r+2, 0x8849CFF7);
        }
        // A receding perspective grid makes the room feel navigable.
        int horizon = (int)(h * .61 + pitch * 25);
        for (int i = 0; i < 15; i++) {
            int yy = horizon + i * i * 2;
            if (yy < h) ctx.fill(0, yy, w, yy + 1, 0x253A55B8);
        }
        for (int i = -10; i <= 10; i++) {
            int bottom = w / 2 + i * 95 - (int)(cameraX * 180);
            ctx.fill(w/2 + (bottom-w/2)/3, horizon, bottom, h, 0x183A55B8);
        }
        // Compact floating glass panel. The cosmic space remains visible around it.
        int pw = Math.min(430, w - 28), ph = Math.min(330, h - 28);
        int x = Math.max(10, Math.min(panelX, w - pw - 10)), y = Math.max(10, Math.min(panelY, h - ph - 10));
        rounded(ctx, x, y, pw, ph, 0xE80A1023, 0xFF39DDF5);
        ctx.fill(x+10,y+10,x+pw-10,y+11,0x5539DDF5);
        ctx.drawText(textRenderer, "AETHER  /  COSMIC SPACE", x+16, y+14, 0xFFB9F7FF, false);
        ctx.drawText(textRenderer, "WASD MOVE  •  SPACE/CTRL UP/DOWN  •  ESC EXIT", x+16, y+28, 0xFF8798C0, false);
        ctx.fill(x+12,y+43,x+pw-12,y+44,0x6639DDF5);
        int sideW = 92, sy = y + 53;
        for (String cat : ModuleRegistry.CATEGORIES) {
            boolean active = category.equals(cat);
            rounded(ctx, x+9, sy-3, sideW-8, 17, active ? 0xFF16475C : 0xC5111A30, active ? 0xFF43DFF6 : 0xFF202C49);
            ctx.drawText(textRenderer, cat, x+15, sy, active ? 0xFFB9FAFF : 0xFF8995B8, false);
            sy += 21;
        }
        int lx = x + sideW + 13, lw = pw - sideW - 29;
        if (selected != null) {
            ctx.drawText(textRenderer, selected.name.toUpperCase(), lx, y+55, 0xFF9FEFFF, false);
            ctx.drawText(textRenderer, selected.description, lx, y+72, 0xFFB7C3E4, false);
            ctx.drawText(textRenderer, "MODULE STATE", lx, y+99, 0xFF8292BA, false);
            settingRow(ctx, lx, y+113, lw, "Enabled", selected.enabled ? "ON" : "OFF", selected.enabled);
            String settingA = settingName(selected, 0), settingB = settingName(selected, 1), settingC = settingName(selected, 2);
            settingRow(ctx, lx, y+139, lw, settingA, (int)selected.amount + (settingA.contains("Range") || settingA.contains("Distance") ? " blocks" : "%"), false);
            ctx.fill(lx, y+160, lx+lw, y+164, 0xFF283450);
            ctx.fill(lx, y+160, lx+(int)(lw * selected.amount / 100.0), y+164, 0xFF39DDF5);
            settingRow(ctx, lx, y+174, lw, settingB, selected.option ? "ON" : "OFF", selected.option);
            settingRow(ctx, lx, y+200, lw, settingC, selected.mode, false);
            ctx.drawText(textRenderer, "Click slider / rows to edit • Right-click to go back", lx, y+235, 0xFF7785AA, false);
        } else {
            ctx.drawText(textRenderer, "MODULE CONSTELLATION  /  " + category.toUpperCase(), lx, y+55, 0xFF9FEFFF, false);
            List<ModuleEntry> visible = new ArrayList<>();
            for (ModuleEntry m : ModuleRegistry.MODULES) if (category.equals("All") || m.category.equals(category)) visible.add(m);
            int rowY = y+75, shown = 0;
            for (int i = Math.max(0, scroll); i < visible.size() && shown < 9; i++, shown++) {
                ModuleEntry m = visible.get(i); boolean hover = mouseX >= lx && mouseX <= lx+lw && mouseY >= rowY-2 && mouseY <= rowY+17;
                rounded(ctx, lx, rowY-2, lw, 19, hover ? 0xFF1A2B49 : 0xD910192D, m.enabled ? 0xFF4DF3C0 : 0xFF24334F);
                ctx.fill(lx+1,rowY,lx+3,rowY+14,m.enabled ? 0xFF4DF3C0 : 0xFF34415F);
                ctx.drawText(textRenderer, m.name, lx+8, rowY+3, m.enabled ? 0xFFB7FFE7 : 0xFFD3DCF7, false);
                ctx.drawText(textRenderer, m.enabled ? "ON" : "OFF", lx+lw-24, rowY+3, m.enabled ? 0xFF55F2BD : 0xFF7884A6, false);
                rowY += 21;
            }
            ctx.drawText(textRenderer, "L-CLICK TOGGLE  •  R-CLICK SETTINGS  •  SCROLL", lx, y+ph-20, 0xFF7E8BB0, false);
        }
        ctx.drawText(textRenderer, "VIRTUAL CAMERA: " + (int)(cameraX*100) + ", " + (int)(cameraY*100), x+13, y+ph-9, 0xFF657395, false);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String settingName(ModuleEntry m, int index) {
        String n = m.name.toLowerCase();
        if (n.contains("zoom")) return index == 0 ? "Zoom level" : index == 1 ? "Smooth zoom" : "Zoom mode";
        if (n.contains("block")) return index == 0 ? "Block range" : index == 1 ? "Show outline" : "Outline style";
        if (n.contains("color") || n.contains("theme")) return index == 0 ? "Color intensity" : index == 1 ? "Animated colors" : "Palette";
        if (n.contains("coord") || n.contains("waypoint")) return index == 0 ? "Marker range" : index == 1 ? "Show labels" : "Marker style";
        if (n.contains("hud") || n.contains("display") || n.contains("counter")) return index == 0 ? "HUD scale" : index == 1 ? "Show background" : "HUD position";
        if (n.contains("particle") || n.contains("cosmic") || n.contains("sky")) return index == 0 ? "Effect density" : index == 1 ? "Animate effects" : "Effect style";
        return index == 0 ? "Feature level" : index == 1 ? "Show details" : "Display mode";
    }
    private void settingRow(DrawContext ctx, int x, int y, int w, String label, String value, boolean on) {
        rounded(ctx, x, y, w, 19, 0xD9131D34, 0xFF263552);
        ctx.drawText(textRenderer, label, x+7, y+5, 0xFFB9C7E7, false);
        int vw = textRenderer.getWidth(value);
        ctx.drawText(textRenderer, value, x+w-vw-7, y+5, on ? 0xFF55F2BD : 0xFF8CA2CA, false);
    }
    private void rounded(DrawContext ctx, int x, int y, int w, int h, int fill, int edge) {
        if (w < 4 || h < 4) return;
        ctx.fill(x+3,y,x+w-3,y+h,fill); ctx.fill(x,y+3,x+w,y+h-3,fill);
        ctx.fill(x+3,y,x+w-3,y+1,edge); ctx.fill(x+3,y+h-1,x+w-3,y+h,edge);
        ctx.fill(x,y+3,x+1,y+h-3,edge); ctx.fill(x+w-1,y+3,x+w,y+h-3,edge);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mx=click.x(), my=click.y(); int b=click.button();
        int pw=Math.min(430,width-28), ph=Math.min(330,height-28), x=Math.max(10,Math.min(panelX,width-pw-10)), y=Math.max(10,Math.min(panelY,height-ph-10));
        if (selected != null) {
            if (b==1) { selected=null; return true; }
            int lx=x+105, lw=pw-121;
            if (my>=y+110 && my<=y+132) selected.enabled=!selected.enabled;
            else if (my>=y+153 && my<=y+168) selected.amount=Math.max(0,Math.min(100,(mx-lx)/(double)lw*100));
            else if (my>=y+171 && my<=y+195) selected.option=!selected.option;
            else if (my>=y+197 && my<=y+222) selected.mode=selected.mode.equals("Default")?"Alternate":selected.mode.equals("Alternate")?"Compact":"Default";
            return true;
        }
        if (my>=y && my<y+45 && mx>=x && mx<x+pw) { if(b==0){dragging=true;dragX=(int)mx-x;dragY=(int)my-y;} return true; }
        if (mx>=x+8 && mx<=x+100 && my>=y+50 && my<=y+50+ModuleRegistry.CATEGORIES.size()*21) {
            int i=(int)((my-(y+50))/21); if(i>=0&&i<ModuleRegistry.CATEGORIES.size()){category=ModuleRegistry.CATEGORIES.get(i);scroll=0;} return true;
        }
        int lx=x+105,lw=pw-121;
        if(mx>=lx&&mx<=lx+lw&&my>=y+72&&my<=y+ph-28){
            List<ModuleEntry> visible=new ArrayList<>(); for(ModuleEntry m:ModuleRegistry.MODULES)if(category.equals("All")||m.category.equals(category))visible.add(m);
            int i=Math.max(0,scroll)+(int)((my-(y+73))/21); if(i<visible.size()){ModuleEntry m=visible.get(i);if(b==1)selected=m;else if(b==0)m.enabled=!m.enabled;} return true;
        }
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseReleased(Click click) { dragging=false; return super.mouseReleased(click); }
    @Override public boolean mouseDragged(Click click,double dx,double dy) { double mx=click.x(),my=click.y(); if(dragging){panelX=(int)mx-dragX;panelY=(int)my-dragY;return true;} yaw+=dx*.003;pitch=Math.max(-1.2,Math.min(1.2,pitch+dy*.003));return true; }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) { if(selected==null)scroll=Math.max(0,Math.min(Math.max(0,ModuleRegistry.MODULES.size()-9),scroll-(int)Math.signum(vertical)*2));else zoom=Math.max(.5,Math.min(2.0,zoom-vertical*.05)); return true; }
    @Override public boolean shouldPause() { return false; }
    @Override public boolean keyPressed(KeyInput input) { int k=input.getKeycode(); if(k==GLFW.GLFW_KEY_ESCAPE){if(selected!=null)selected=null;else close();return true;} if(k==GLFW.GLFW_KEY_RIGHT_SHIFT){close();return true;} return super.keyPressed(input); }
    private record Star(double x,double y,double depth,double phase) {}
    private record Orb(double x,double y,double depth,double radius) {}
}
