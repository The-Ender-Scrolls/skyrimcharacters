package com.ryankshah.skyrimcharacters.client.screen;

import com.ryankshah.skyrimcharacters.Constants;
import com.ryankshah.skyrimcharacters.data.PlayerCharacter;
import com.ryankshah.skyrimcharacters.data.Race;
import com.ryankshah.skyrimcharacters.network.UpdatePlayerCharacter;
import commonnetwork.api.Dispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CharacterCreationScreen extends Screen
{
    private enum CreationTab {
        RACE("Race"),
        SEX("Sex"),
        BODY("Body"),
        HEAD("Head"),
        FACE("Face");

        private final String name;

        CreationTab(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum CameraFocus {
        FULL_BODY(0.0f, 30),
        HEAD(1.2f, 60),
        FACE(1.4f, 80),
        BODY(0.5f, 40);

        private final float yOffset;
        private final int scale;

        CameraFocus(float yOffset, int scale) {
            this.yOffset = yOffset;
            this.scale = scale;
        }
    }

    private CreationTab currentTab = CreationTab.RACE;
    private CameraFocus currentFocus = CameraFocus.FULL_BODY;

    // Character customization options
    private int selectedRace = 0;
    private boolean isMale = true;
    private float skinTone = 0.5f;  // 0.0 to 1.0
    private float hairStyle = 0.0f;  // 0.0 to 1.0 (discretized to styles)
    private float hairColor = 0.0f;  // 0.0 to 1.0
    private float eyeColor = 0.0f;   // 0.0 to 1.0
    private float noseShape = 0.5f;  // 0.0 to 1.0
    private float mouthShape = 0.5f; // 0.0 to 1.0
    private float browShape = 0.5f;  // 0.0 to 1.0

    // Animation
    private float modelRotation = 0.0f;
    private float targetRotation = 0.0f;
    private float cameraTransitionProgress = 1.0f;
    private CameraFocus previousFocus = CameraFocus.FULL_BODY;

    // Slider interaction
    private CharacterSlider activeSlider = null;
    private boolean isDraggingSlider = false;

    // Mouse tracking for model rotation
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean isDraggingModel = false;

    // UI Layout
    private List<CharacterSlider> sliders = new ArrayList<>();
    private static final String[] RACES = {"Nord", "Imperial", "Breton", "Redguard", "Dark Elf", "High Elf", "Wood Elf", "Orc", "Khajiit", "Argonian"};

    // Tab click detection
    private int tabWidth = 80;
    private int tabHeight = 30;

    public CharacterCreationScreen() {
        super(Component.translatable(Constants.MOD_ID + ".creationscreen.title"));
    }

    @Override
    protected void init() {
        super.init();
        tabWidth = Math.min(80, this.width / 6);
        updateSliders();
    }

    private void selectTab(CreationTab tab) {
        if (this.currentTab != tab) {
            this.currentTab = tab;

            previousFocus = currentFocus;
            switch (tab) {
                case RACE:
                case SEX:
                case BODY:
                    currentFocus = CameraFocus.FULL_BODY;
                    break;
                case HEAD:
                    currentFocus = CameraFocus.HEAD;
                    break;
                case FACE:
                    currentFocus = CameraFocus.FACE;
                    break;
            }

            cameraTransitionProgress = 0.0f;
            updateSliders();
        }
    }

    private void updateSliders() {
        sliders.clear();

        int sidePanelWidth = Math.min(220, this.width / 4);
        int sliderX = this.width - sidePanelWidth - 15;
        int sliderWidth = sidePanelWidth - 10;
        int startY = 80;
        int spacing = 50;

        switch (currentTab) {
            case RACE:
                // Race selection (discrete slider)
                sliders.add(new CharacterSlider(sliderX, startY, sliderWidth, "Race",
                        () -> (float)selectedRace / (RACES.length - 1),
                        (val) -> {
                            selectedRace = Math.round(val * (RACES.length - 1));
                        },
                        () -> RACES[selectedRace]
                ));
                break;
            case SEX:
                // Gender toggle (binary slider)
                sliders.add(new CharacterSlider(sliderX, startY, sliderWidth, "Sex",
                        () -> isMale ? 0.0f : 1.0f,
                        (val) -> isMale = val < 0.5f,
                        () -> isMale ? "Male" : "Female"
                ));
                break;
            case BODY:
                sliders.add(new CharacterSlider(sliderX, startY, sliderWidth, "Skin Tone",
                        () -> skinTone,
                        (val) -> skinTone = val,
                        null
                ));
                break;
            case HEAD:
                sliders.add(new CharacterSlider(sliderX, startY, sliderWidth, "Hair Style",
                        () -> hairStyle,
                        (val) -> hairStyle = val,
                        () -> String.valueOf(Math.round(hairStyle * 10))
                ));
                sliders.add(new CharacterSlider(sliderX, startY + spacing, sliderWidth, "Hair Color",
                        () -> hairColor,
                        (val) -> hairColor = val,
                        null
                ));
                break;
            case FACE:
                sliders.add(new CharacterSlider(sliderX, startY, sliderWidth, "Eye Color",
                        () -> eyeColor,
                        (val) -> eyeColor = val,
                        null
                ));
                sliders.add(new CharacterSlider(sliderX, startY + spacing, sliderWidth, "Nose Shape",
                        () -> noseShape,
                        (val) -> noseShape = val,
                        null
                ));
                sliders.add(new CharacterSlider(sliderX, startY + spacing * 2, sliderWidth, "Mouth Shape",
                        () -> mouthShape,
                        (val) -> mouthShape = val,
                        null
                ));
                sliders.add(new CharacterSlider(sliderX, startY + spacing * 3, sliderWidth, "Brow Shape",
                        () -> browShape,
                        (val) -> browShape = val,
                        null
                ));
                break;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
//        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        updateAnimations(partialTick);
        renderCharacterModel(graphics, mouseX, mouseY, partialTick);
        renderPanels(graphics);
        renderTabs(graphics, mouseX, mouseY);
        renderSliders(graphics, mouseX, mouseY);
        renderButtons(graphics, mouseX, mouseY);
    }

    private void updateAnimations(float partialTick) {
        if (Math.abs(targetRotation - modelRotation) > 0.1f) {
            modelRotation += (targetRotation - modelRotation) * 0.1f * partialTick;
        }

        if (cameraTransitionProgress < 1.0f) {
            cameraTransitionProgress = Math.min(1.0f, cameraTransitionProgress + 0.05f * partialTick);
        }
    }

    private void renderCharacterModel(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) return;

        float yOffset = lerp(previousFocus.yOffset, currentFocus.yOffset, smoothStep(cameraTransitionProgress));
        float scale = lerp(previousFocus.scale, currentFocus.scale, smoothStep(cameraTransitionProgress));

        int sidePanelWidth = Math.min(220, this.width / 4);
        int modelAreaWidth = Math.min(400, this.width - sidePanelWidth - 100);
        int modelAreaHeight = this.height - 120;

        int modelCenterX = (this.width - sidePanelWidth) / 2;

        int x1 = modelCenterX - modelAreaWidth / 2;
        int y1 = 60;
        int x2 = modelCenterX + modelAreaWidth / 2;
        int y2 = y1 + modelAreaHeight;

        LivingEntity player = minecraft.player;
        renderEntityInInventoryStatic(graphics, x1, y1, x2, y2, (int)scale, yOffset, player);
    }

    private void renderPanels(GuiGraphics graphics) {
        // Top tab background
        graphics.fillGradient(0, 15, this.width, 55, 0xCC000000, 0xCC000000);
        graphics.fillGradient(0, 54, this.width, 56, 0xFF6E6B64, 0xFF6E6B64);

        // Right side panel
        int sidePanelWidth = Math.min(220, this.width / 4);
        int panelX = this.width - sidePanelWidth - 30;
        drawBorderedGradientRect(graphics, panelX, 60, this.width - 10, this.height - 50, 0xAA000000, 0xAA000000, 0xFF6E6B64);

        // Bottom bar
        graphics.fillGradient(0, this.height - 50, this.width, this.height, 0xCC000000, 0xCC000000);
        graphics.fillGradient(0, this.height - 50, this.width, this.height - 48, 0xFF6E6B64, 0xFF6E6B64);
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int totalTabsWidth = tabWidth * CreationTab.values().length;
        int tabStartX = this.width / 2 - totalTabsWidth / 2;
        int tabY = 20;

        for (int i = 0; i < CreationTab.values().length; i++) {
            CreationTab tab = CreationTab.values()[i];
            int x = tabStartX + (i * tabWidth);

            boolean isActive = i == currentTab.ordinal();
            boolean isHovered = mouseX >= x && mouseX <= x + tabWidth - 2 &&
                    mouseY >= tabY && mouseY <= tabY + tabHeight;

            int color = isActive ? 0xFFFFFFFF : (isHovered ? 0xFFFFFFAA : 0xFFC0C0C0);

            graphics.drawCenteredString(font, tab.getName(), x + (tabWidth - 2) / 2, tabY + 10, color);

            if (isActive) {
                graphics.fill(x, tabY + tabHeight - 2, x + tabWidth - 2, tabY + tabHeight, 0xFFFFFFFF);
            }
        }
    }

    private void renderSliders(GuiGraphics graphics, int mouseX, int mouseY) {
        for (CharacterSlider slider : sliders) {
            slider.render(graphics, font, mouseX, mouseY);
        }
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        int buttonY = this.height - 25;
        int xOffset = 20;

        // "R" key - Random button
        drawBorderedGradientRect(graphics, xOffset, buttonY - 5, xOffset + 15 + font.width("Random"), buttonY + 10,
                0xAA000000, 0xAA000000, 0xFF6E6B64);
        graphics.drawString(font, "R", xOffset + 5, buttonY, 0xFFFFFFFF);

        boolean isHoveringRandom = mouseX >= xOffset + 15 && mouseX <= xOffset + 15 + font.width("Random") &&
                mouseY >= buttonY && mouseY <= buttonY + 9;
        int randomColor = isHoveringRandom ? 0xFFFFFFAA : 0xFFFFFFFF;
        graphics.drawString(font, "Random", xOffset + 15, buttonY, randomColor);

        xOffset += 20 + font.width("Random");

        // "Enter" key - Done button
        drawBorderedGradientRect(graphics, xOffset, buttonY - 5, xOffset + 15 + font.width("Done"), buttonY + 10,
                0xAA000000, 0xAA000000, 0xFF6E6B64);
        graphics.drawString(font, "Enter", xOffset + 5, buttonY, 0xFFFFFFFF);

        boolean isHoveringDone = mouseX >= xOffset + 20 && mouseX <= xOffset + 20 + font.width("Done") &&
                mouseY >= buttonY && mouseY <= buttonY + 9;
        int doneColor = isHoveringDone ? 0xFFFFFFAA : 0xFFFFFFFF;
        graphics.drawString(font, "Done", xOffset + 20, buttonY, doneColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0) {
            int mouseX = (int)event.x();
            int mouseY = (int)event.y();

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            // Check tab clicks
            int totalTabsWidth = tabWidth * CreationTab.values().length;
            int tabStartX = this.width / 2 - totalTabsWidth / 2;
            int tabY = 20;

            for (int i = 0; i < CreationTab.values().length; i++) {
                int x = tabStartX + (i * tabWidth);
                if (mouseX >= x && mouseX <= x + tabWidth - 2 &&
                        mouseY >= tabY && mouseY <= tabY + tabHeight) {
                    selectTab(CreationTab.values()[i]);
                    return true;
                }
            }

            // Check slider clicks
            for (CharacterSlider slider : sliders) {
                if (slider.isMouseOver(mouseX, mouseY)) {
                    activeSlider = slider;
                    isDraggingSlider = true;
                    slider.onClick(mouseX);
                    return true;
                }
            }

            // Check button clicks
            int buttonY = this.height - 25;
            int xOffset = 20;

            // Random button
            int randomButtonX = xOffset + 15;
            if (mouseX >= randomButtonX && mouseX <= randomButtonX + font.width("Random") &&
                    mouseY >= buttonY && mouseY <= buttonY + 9) {
                randomizeCharacter();
                return true;
            }

            xOffset += 20 + font.width("Random");

            // Done button
            int doneButtonX = xOffset + 20;
            if (mouseX >= doneButtonX && mouseX <= doneButtonX + font.width("Done") &&
                    mouseY >= buttonY && mouseY <= buttonY + 9) {
                finishCharacterCreation();
                return true;
            }

            // If not clicking on UI elements, start model drag
            isDraggingModel = true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (event.button() == 0) {
            if (isDraggingSlider && activeSlider != null) {
                activeSlider.onDrag((int)mouseX);
                return true;
            } else if (isDraggingModel) {
                // Allow rotating the model by dragging
                targetRotation += (float)(mouseX - lastMouseX) * 2.0f;
            }
        }
        lastMouseX = (int)mouseX;
        lastMouseY = (int)mouseY;
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            isDraggingSlider = false;
            isDraggingModel = false;
            activeSlider = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            finishCharacterCreation();
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_R) {
            randomizeCharacter();
            return true;
        }
        return super.keyPressed(event);
    }

    private void randomizeCharacter() {
        selectedRace = (int)(Math.random() * RACES.length);
        isMale = Math.random() < 0.5;
        skinTone = (float)Math.random();
        hairStyle = (float)Math.random();
        hairColor = (float)Math.random();
        eyeColor = (float)Math.random();
        noseShape = (float)Math.random();
        mouthShape = (float)Math.random();
        browShape = (float)Math.random();
        updateSliders();
    }

    private void finishCharacterCreation() {
        // Map selected race index to Race object
        Race selectedRaceObject = Race.getRaces().get(selectedRace);

        // Create PlayerCharacter with all customization data and characterCreated flag set to true
        PlayerCharacter character = new PlayerCharacter(
                true,                   // characterCreated
                selectedRaceObject,     // race
                isMale,                 // isMale
                skinTone,               // skinTone
                hairStyle,              // hairStyle
                hairColor,              // hairColor
                eyeColor,               // eyeColor
                noseShape,              // noseShape
                mouthShape,             // mouthShape
                browShape               // browShape
        );

        // Send character data to server
        Dispatcher.sendToServer(new UpdatePlayerCharacter(character));

        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void drawBorderedGradientRect(GuiGraphics graphics, int startX, int startY, int endX, int endY,
                                          int colorStart, int colorEnd, int borderColor) {
        graphics.fillGradient(startX, startY, endX, endY, colorStart, colorEnd);
        graphics.fill(startX, startY, endX, startY + 1, borderColor);
        graphics.fill(startX, endY - 1, endX, endY, borderColor);
        graphics.fill(startX, startY + 1, startX + 1, endY - 1, borderColor);
        graphics.fill(endX - 1, startY + 1, endX, endY - 1, borderColor);
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private float smoothStep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    private void renderEntityInInventoryStatic(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2,
                                               int scale, float yOffset, LivingEntity entity) {
        guiGraphics.enableScissor(x1, y1, x2, y2);

        Quaternionf baseRotation = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf cameraRotation = new Quaternionf().rotateX(0.0F);

        float oldBodyRot = entity.yBodyRot;
        float oldYRot = entity.getYRot();
        float oldXRot = entity.getXRot();
        float oldHeadRotO = entity.yHeadRotO;
        float oldHeadRot = entity.yHeadRot;

        entity.yBodyRot = 180.0F + modelRotation;
        entity.setYRot(180.0F + modelRotation);
        entity.setXRot(0.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float entityScale = entity.getScale();
        Vector3f translation = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + yOffset * entityScale, 0.0F);
        float renderScale = (float)scale / entityScale;

        renderEntityInInventory(guiGraphics, x1, y1, x2, y2, renderScale, translation,
                baseRotation, cameraRotation, entity);

        entity.yBodyRot = oldBodyRot;
        entity.setYRot(oldYRot);
        entity.setXRot(oldXRot);
        entity.yHeadRotO = oldHeadRotO;
        entity.yHeadRot = oldHeadRot;

        guiGraphics.disableScissor();
    }

    private void renderEntityInInventory(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2,
                                         float scale, Vector3f translation, Quaternionf rotation,
                                         @Nullable Quaternionf cameraRotation, LivingEntity entity) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.lightCoords = 15728880;
        renderState.hitboxesRenderState = null;
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        guiGraphics.submitEntityRenderState(renderState, scale, translation, rotation,
                cameraRotation, x1, y1, x2, y2);
    }

    // Inner class for sliders
    private static class CharacterSlider {
        private final int x, y, width;
        private final String label;
        private final SliderValueGetter getter;
        private final SliderValueSetter setter;
        private final SliderDisplayText displayText;

        private static final int SLIDER_HEIGHT = 4;
        private static final int HANDLE_WIDTH = 8;
        private static final int HANDLE_HEIGHT = 12;

        public CharacterSlider(int x, int y, int width, String label,
                               SliderValueGetter getter, SliderValueSetter setter,
                               @Nullable SliderDisplayText displayText) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.displayText = displayText;
        }

        public void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY) {
            // Draw label
            graphics.drawString(font, label, x, y - 15, 0xFFFFFFFF);

            // Draw value if custom display text is provided
            if (displayText != null) {
                String valueText = displayText.getText();
                graphics.drawString(font, valueText, x + width - font.width(valueText), y - 15, 0xFFC0C0C0);
            }

            // Draw slider track
            graphics.fill(x, y + (HANDLE_HEIGHT - SLIDER_HEIGHT) / 2,
                    x + width, y + (HANDLE_HEIGHT - SLIDER_HEIGHT) / 2 + SLIDER_HEIGHT,
                    0xFF3A3A3A);

            // Draw filled portion
            float value = getter.getValue();
            int fillWidth = (int)(width * value);
            graphics.fill(x, y + (HANDLE_HEIGHT - SLIDER_HEIGHT) / 2,
                    x + fillWidth, y + (HANDLE_HEIGHT - SLIDER_HEIGHT) / 2 + SLIDER_HEIGHT,
                    0xFF6E6B64);

            // Draw slider handle
            int handleX = x + (int)((width - HANDLE_WIDTH) * value);
            boolean isHovered = isMouseOver(mouseX, mouseY);
            int handleColor = isHovered ? 0xFFFFFFFF : 0xFFD0D0D0;

            graphics.fill(handleX, y, handleX + HANDLE_WIDTH, y + HANDLE_HEIGHT, handleColor);
            graphics.fill(handleX + 1, y + 1, handleX + HANDLE_WIDTH - 1, y + HANDLE_HEIGHT - 1, 0xFF1A1A1A);
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y - 15 && mouseY <= y + HANDLE_HEIGHT + 5;
        }

        public void onClick(int mouseX) {
            updateValue(mouseX);
        }

        public void onDrag(int mouseX) {
            updateValue(mouseX);
        }

        private void updateValue(int mouseX) {
            float newValue = (float)(mouseX - x) / width;
            newValue = Math.max(0.0f, Math.min(1.0f, newValue));
            setter.setValue(newValue);
        }

        @FunctionalInterface
        interface SliderValueGetter {
            float getValue();
        }

        @FunctionalInterface
        interface SliderValueSetter {
            void setValue(float value);
        }

        @FunctionalInterface
        interface SliderDisplayText {
            String getText();
        }
    }
}