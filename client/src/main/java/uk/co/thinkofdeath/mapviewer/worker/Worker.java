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

package uk.co.thinkofdeath.mapviewer.worker;


import com.google.gwt.core.client.EntryPoint;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.WorkerGlobalScope;
import uk.co.thinkofdeath.mapviewer.shared.IMapViewer;
import uk.co.thinkofdeath.mapviewer.shared.block.BlockRegistry;
import uk.co.thinkofdeath.mapviewer.shared.logging.LoggerFactory;
import uk.co.thinkofdeath.mapviewer.shared.worker.ChunkLoadMessage;
import uk.co.thinkofdeath.mapviewer.shared.worker.WorkerMessage;
import uk.co.thinkofdeath.mapviewer.worker.world.WorkerChunk;
import uk.co.thinkofdeath.mapviewer.worker.world.WorkerWorld;

public class Worker implements EntryPoint, EventListener, IMapViewer {

    private final LoggerFactory loggerFactory = new WorkerLogger();
    private final BlockRegistry blockRegistry = new BlockRegistry(this);
    private final WorkerWorld world = new WorkerWorld(this);

    @Override
    public void onModuleLoad() {
        setOnmessage(this);
        getBlockRegistry().init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event evt) {
        MessageEvent event = (MessageEvent) evt;
        WorkerMessage message = (WorkerMessage) event.getData();

        switch (message.getType()) {
            case "chunk:load":
                ChunkLoadMessage chunkLoadMessage = (ChunkLoadMessage) message.getMessage();
                world.addChunk(new WorkerChunk(world,
                        chunkLoadMessage.getX(), chunkLoadMessage.getZ(),
                        chunkLoadMessage.getData(), message.getReturn()));
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    // Web worker magic

    /**
     * Returns the web worker global scope for this worker
     *
     * @return The global scope
     */
    public native WorkerGlobalScope getSelf()/*-{
        return $wnd;
    }-*/;

    /**
     * Posts a message back to the creator of this worker
     *
     * @param message The message to send
     */
    public native void postMessage(Object message)/*-{
        $wnd.postMessage(message);
    }-*/;

    /**
     * Posts a message back to the creator of this worker
     * and transfers all of the transferables
     *
     * @param message       The message to send
     * @param transferables The transferables to send
     */
    public native void postMessage(Object message, Object[] transferables)/*-{
        $wnd.postMessage(message, transferables);
    }-*/;

    private native void setOnmessage(EventListener eventListener)/*-{
        self.onmessage = @elemental.js.dom.JsElementalMixinBase::getHandlerFor(Lelemental/events/EventListener;)(eventListener);
    }-*/;
}