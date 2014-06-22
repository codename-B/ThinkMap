/*
 * Copyright 2014 Matthew Collins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.thinkofdeath.thinkcraft.html.client.input;

import elemental.events.Touch;
import elemental.events.TouchList;
import uk.co.thinkofdeath.thinkcraft.html.client.MapViewer;
import uk.co.thinkofdeath.thinkcraft.html.client.render.Camera;
import uk.co.thinkofdeath.thinkcraft.shared.collision.AABB;
import uk.co.thinkofdeath.thinkcraft.shared.vector.Vector3;

public class InputManager {

    private static final double EYE_HEIGHT = 1.6;

    private final MapViewer mapViewer;

    private int movingDirection = 0;

    private float lx = Float.NaN;
    private float ly = Float.NaN;
    private float lz = Float.NaN;

    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    private Vector3 direction = new Vector3();
    private float vSpeed = 0;
    private boolean onGround = false;
    private int offGround = 0;

    /**
     * Creates a new input manager
     *
     * @param mapViewer
     *         The map viewer that owns it
     */
    public InputManager(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    /**
     * Updates the input manager
     *
     * @param delta
     */
    public void update(double delta) {
        Camera camera = mapViewer.getCamera();

        if (Float.isNaN(lx)) {
            lx = camera.getX();
            ly = camera.getY();
            lz = camera.getZ();
        }

        if (movingDirection != 0) {
            camera.setX((float) (camera.getX() + 0.1 * Math.sin(camera.getRotationY()) * delta * movingDirection));
            camera.setZ((float) (camera.getZ() - 0.1 * Math.cos(camera.getRotationY()) * delta * movingDirection));
        }
        camera.setY(camera.getY() + vSpeed);

        vSpeed -= 0.01;
        if (vSpeed < -0.5f) vSpeed = -0.5f;

        // Collisions

        // X Axis
        hitbox.set(camera.getX() - 0.2, ly - EYE_HEIGHT, lz - 0.2,
                camera.getX() + 0.2, ly + 0.2, lz + 0.2);
        direction.set(camera.getX() - lx, 0, 0);
        checkCollisions();
        camera.setX((float) (hitbox.getX1() + 0.2));

        // Z Axis
        hitbox.set(camera.getX() - 0.2, ly - EYE_HEIGHT, camera.getZ() - 0.2,
                camera.getX() + 0.2, ly + 0.2, camera.getZ() + 0.2);
        direction.set(0, 0, camera.getZ() - lz);
        checkCollisions();
        camera.setZ((float) (hitbox.getZ1() + 0.2));

        // Y Axis
        hitbox.set(camera.getX() - 0.2, camera.getY() - EYE_HEIGHT, camera.getZ() - 0.2,
                camera.getX() + 0.2, camera.getY() + 0.2, camera.getZ() + 0.2);
        direction.set(0, camera.getY() - ly, 0);
        if (checkCollisions()) {
            vSpeed = 0;
            onGround = true;
            offGround = 0;
        } else {
            onGround = offGround <= 1;
            offGround++;
        }
        camera.setY((float) (hitbox.getY1() + EYE_HEIGHT));

        lx = camera.getX();
        ly = camera.getY();
        lz = camera.getZ();

        while (mapViewer.getWorld().isLoaded((int) camera.getX() >> 4, (int) camera.getZ() >> 4) && checkCollisions()) {
            camera.setY(camera.getY() + 0.1f);
            hitbox.set(camera.getX() - 0.2, camera.getY() - EYE_HEIGHT, camera.getZ() - 0.2,
                    camera.getX() + 0.2, camera.getY() + 0.2, camera.getZ() + 0.2);
        }
    }

    private boolean checkCollisions() {
        int mix = (int) (hitbox.getX1() - 1);
        int max = (int) (hitbox.getX2() + 1);
        int miy = (int) (hitbox.getY1() - 1);
        int may = (int) (hitbox.getY2() + 1);
        int miz = (int) (hitbox.getZ1() - 1);
        int maz = (int) (hitbox.getZ2() + 1);

        boolean hit = false;
        for (int y = miy; y < may; y++) {
            for (int z = miz; z < maz; z++) {
                for (int x = mix; x < max; x++) {
                    hit |= mapViewer.getWorld().getBlock(x, y, z).collide(hitbox, x, y, z, direction);
                }
            }
        }
        return hit;
    }

    // This is where things get nasty.
    // Supporting multiple browsers and devices can be a pain since some browsers may use
    // prefixed events, some may use different events and others will support the event but
    // not the same variables as other browsers.
    // TODO: Drag controls

    /**
     * Causes the input manager to create its hooks into the browsers events
     */
    public void hook() {
        registerNativeEvents();
    }

    private void onCanvasClick(int x, int y) {
        if (!isPointerLocked()) {
            requestPointerLock();
        }
    }

    private void onMouseLockMove(int movementX, int movementY) {
        if (!isPointerLocked()) {
            return;
        }
        // Filter overly large movements
        if (Math.abs(movementX) > 300 || Math.abs(movementY) > 300) return;

        Camera camera = mapViewer.getCamera();
        camera.setRotationY(camera.getRotationY() + (movementX * 0.0025f));
        camera.setRotationX(camera.getRotationX() + (movementY * 0.0025f));
        if (camera.getRotationX() > Math.PI / 2) {
            camera.setRotationX((float) (Math.PI / 2));
        }
        if (camera.getRotationX() < -Math.PI / 2) {
            camera.setRotationX((float) (-Math.PI / 2));
        }

        while (camera.getRotationY() < 0) {
            camera.setRotationY((float) (camera.getRotationY() + Math.PI * 2));
        }
        while (camera.getRotationY() >= Math.PI * 2) {
            camera.setRotationY((float) (camera.getRotationY() - Math.PI * 2));
        }
    }

    private void onMouseMove(int x, int y) {

    }

    private boolean onKeyDown(char keyCode) {
        switch (keyCode) {
            case 'W':
                movingDirection = 1;
                return true;
            case 'S':
                movingDirection = -1;
                return true;
            case 32:
                if (onGround) {
                    vSpeed = 0.15f;
                }
                return true;
        }
        return false;
    }

    private boolean onKeyUp(char keyCode) {
        switch (keyCode) {
            case 'W':
                if (movingDirection == 1) {
                    movingDirection = 0;
                }
                return true;
            case 'S':
                if (movingDirection == -1) {
                    movingDirection = 0;
                }
                return true;
        }
        return false;
    }

    private int cameraTouch = -1;
    private int cameraTouchX = 0;
    private int cameraTouchY = 0;
    private int moveTouch = -1;

    private void onTouchStart(TouchList touches) {
        int offset = 0;
        while (cameraTouch == -1 && touches.getLength() - offset >= 1) {
            if (touches.item(offset).getIdentifier() != moveTouch) { // TODO: Improve
                Touch touch = touches.item(offset++);
                cameraTouchX = touch.getClientX();
                cameraTouchY = touch.getClientY();
                cameraTouch = touch.getIdentifier();
                break;
            }
            offset++;
        }
        while (moveTouch == -1 && touches.getLength() - offset >= 1) {
            if (touches.item(offset).getIdentifier() != cameraTouch) { // TODO: Improve
                moveTouch = touches.item(offset++).getIdentifier();
                movingDirection = 1;
            }
            offset++;
        }
    }

    private void onTouchEnd(TouchList touches) {
        Touch ct = findTouch(touches, cameraTouch);
        if (ct == null) {
            cameraTouch = -1;
            cameraTouchX = 0;
            cameraTouchY = 0;
        }
        Touch mt = findTouch(touches, moveTouch);
        if (mt == null) {
            movingDirection = 0;
            moveTouch = -1;
        }
    }

    private void onTouchMove(TouchList touches) {
        Touch ct = findTouch(touches, cameraTouch);
        if (ct != null) {
            Camera camera = mapViewer.getCamera();
            float movementX = ct.getClientX() - cameraTouchX;
            float movementY = ct.getClientY() - cameraTouchY;

            camera.setRotationY(camera.getRotationY() + (movementX * 0.02f));
            camera.setRotationX(camera.getRotationX() + (movementY * 0.02f));
            if (camera.getRotationX() > Math.PI / 2) {
                camera.setRotationX((float) (Math.PI / 2));
            }
            if (camera.getRotationX() < -Math.PI / 2) {
                camera.setRotationX((float) (-Math.PI / 2));
            }

            while (camera.getRotationY() < 0) {
                camera.setRotationY((float) (camera.getRotationY() + Math.PI * 2));
            }
            while (camera.getRotationY() >= Math.PI * 2) {
                camera.setRotationY((float) (camera.getRotationY() - Math.PI * 2));
            }

            cameraTouchX = ct.getClientX();
            cameraTouchY = ct.getClientY();
        }
    }

    private native Touch findTouch(TouchList touches, int id)/*-{
        for (var i = 0; i < touches.length; i++) {
            if (touches[i].identifier === id) {
                return touches[i];
            }
        }
        return null;
    }-*/;

    private native boolean isPointerLocked()/*-{
        return ($doc.pointerLockElement
            || $doc.webkitPointerLockElement
            || $doc.mozPointerLockElement) != null;
    }-*/;

    private native boolean requestPointerLock()/*-{
        var canvas = $doc.getElementById("main");
        canvas.requestPointerLock = (canvas.requestPointerLock
            || canvas.webkitRequestPointerLock
            || canvas.mozRequestPointerLock);
        if (canvas.requestPointerLock == null) {
            return false;
        }
        canvas.requestPointerLock();
        return true;
    }-*/;


    private native void registerNativeEvents()/*-{
        var that = this;
        var canvas = $doc.getElementById("main");
        canvas.addEventListener("click", function (e) {
            e.preventDefault();
            that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onCanvasClick(II)(e.clientX, e.clientY);
        }, false);
        canvas.addEventListener("mousemove", function (e) {
            var mx = (e.movementX || e.webkitMovementX || e.mozMovementX);
            var my = (e.movementY || e.webkitMovementY || e.mozMovementY);
            if (($doc.pointerLockElement
                || $doc.webkitPointerLockElement
                || $doc.mozPointerLockElement) === null || mx === undefined || my === undefined) {
                that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onMouseMove(II)(e.clientX, e.clientY);
            } else {
                that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onMouseLockMove(II)(mx, my);
            }
            e.stopPropagation();
            e.preventDefault();
        }, false);
        $doc.addEventListener("keydown", function (e) {
            var key = e.keyCode || e.which;
            if (that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onKeyDown(C)(key)) {
                e.preventDefault();
            }
        }, false);
        $doc.addEventListener("keyup", function (e) {
            var key = e.keyCode || e.which;
            if (that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onKeyUp(C)(key)) {
                e.preventDefault();
            }
        }, false);

        // Mobile
        $doc.addEventListener("touchstart", function (e) {
            e.preventDefault();
            that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onTouchStart(Lelemental/events/TouchList;)(e.touches);
        });
        $doc.addEventListener("touchend", function (e) {
            e.preventDefault();
            that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onTouchEnd(Lelemental/events/TouchList;)(e.touches);
        });
        $doc.addEventListener("touchmove", function (e) {
            e.preventDefault();
            that.@uk.co.thinkofdeath.thinkcraft.html.client.input.InputManager::onTouchMove(Lelemental/events/TouchList;)(e.touches);
        }, false);
    }-*/;
}
