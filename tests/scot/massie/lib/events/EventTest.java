package scot.massie.lib.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.utils.wrappers.MutableWrapper;
import scot.massie.lib.utils.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

abstract class EventTest<T extends Event<EventTest.TestEventArgs>>
{
    public static final class TestEventArgs implements EventArgs
    {
        public TestEventArgs(String text)
        { this.text = text; }

        private final String text;

        public String getText()
        { return text; }
    }

    private static final class TestFlag
    {
        public TestFlag(String initialText)
        { this.text = initialText; }

        public TestFlag()
        { this("Flag not set."); }

        String text;
        final EventListener<TestEventArgs> settingEventListener = args -> text = args.getText();

        public void set(String newText)
        { this.text = newText; }

        public String get()
        { return text; }

        public EventListener<TestEventArgs> getSetListener()
        { return settingEventListener; }

        public void assertEquals(String expected)
        { Assertions.assertEquals(expected, text); }
    }

    private static final class FlagList implements Iterable<TestFlag>
    {
        public FlagList(int size)
        {
            for(int i = 0; i < size; i++)
                flags.add(new TestFlag());
        }

        List<TestFlag> flags = new ArrayList<>();

        public TestFlag get(int n)
        { return flags.get(n); }

        public void assertAllEquals(String expected)
        {
            for(TestFlag f : flags)
                Assertions.assertEquals(expected, f.get());
        }

        @Override
        public Iterator<TestFlag> iterator()
        { return flags.iterator(); }
    }

    public abstract T getNewEvent();

    @Test
    public void registerListenerAndInvoke()
    {
        T e = getNewEvent();
        FlagList fl = new FlagList(3);

        for(TestFlag f : fl)
            e.register(f.getSetListener());

        e.invoke(new TestEventArgs("red"));
        fl.assertAllEquals("red");

        e.invoke(new TestEventArgs("blue"));
        fl.assertAllEquals("blue");
    }

    @Test
    public void deregisterListener()
    {
        T e = getNewEvent();
        FlagList fl = new FlagList(4);

        for(TestFlag f : fl)
            e.register(f.getSetListener());

        e.invoke(new TestEventArgs("red"));
        fl.assertAllEquals("red");

        e.invoke(new TestEventArgs("yellow"));
        fl.assertAllEquals("yellow");

        e.deregister(fl.get(0).getSetListener());
        e.deregister(fl.get(1).getSetListener());
        e.invoke(new TestEventArgs("pink"));
        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("pink");
        fl.get(3).assertEquals("pink");

        e.deregister(fl.get(3).getSetListener());
        e.invoke(new TestEventArgs("green"));
        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("green");
        fl.get(3).assertEquals("pink");
    }

    @Test
    public void clearListeners()
    {
        T e = getNewEvent();
        FlagList fl = new FlagList(3);

        for(TestFlag f : fl)
            e.register(f.getSetListener());

        e.invoke(new TestEventArgs("yellow"));
        fl.assertAllEquals("yellow");

        e.clearListeners();
        e.invoke(new TestEventArgs("red"));
        fl.assertAllEquals("yellow");
    }

    @Test
    public void registerDependentEvent()
    {
        T parentEvent = getNewEvent();
        T e1 = getNewEvent();
        T e2 = getNewEvent();
        T e3 = getNewEvent();
        FlagList fl = new FlagList(4);

        parentEvent.register(fl.get(0).getSetListener());
        e1.register(fl.get(1).getSetListener());
        e2.register(fl.get(2).getSetListener());
        e3.register(fl.get(3).getSetListener());

        parentEvent.register(e1);
        parentEvent.register(e2);
        parentEvent.register(e3, x -> new TestEventArgs(x.getText() + " for e3"));

        parentEvent.invoke(new TestEventArgs("yellow"));

        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");
    }

    @Test
    public void deregisterDependentEvent()
    {
        T parentEvent = getNewEvent();
        T e1 = getNewEvent();
        T e2 = getNewEvent();
        T e3 = getNewEvent();
        FlagList fl = new FlagList(4);

        parentEvent.register(fl.get(0).getSetListener());
        e1.register(fl.get(1).getSetListener());
        e2.register(fl.get(2).getSetListener());
        e3.register(fl.get(3).getSetListener());

        parentEvent.register(e1);
        parentEvent.register(e2);
        parentEvent.register(e3, x -> new TestEventArgs(x.getText() + " for e3"));

        parentEvent.invoke(new TestEventArgs("yellow"));

        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");

        parentEvent.deregister(e2);
        parentEvent.invoke(new TestEventArgs("red"));

        fl.get(0).assertEquals("red");
        fl.get(1).assertEquals("red");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("red for e3");

        parentEvent.deregister(e3);
        parentEvent.invoke(new TestEventArgs("green"));

        fl.get(0).assertEquals("green");
        fl.get(1).assertEquals("green");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("red for e3");
    }

    @Test
    public void clearDependentEvents()
    {
        T parentEvent = getNewEvent();
        T e1 = getNewEvent();
        T e2 = getNewEvent();
        T e3 = getNewEvent();
        FlagList fl = new FlagList(4);

        parentEvent.register(fl.get(0).getSetListener());
        e1.register(fl.get(1).getSetListener());
        e2.register(fl.get(2).getSetListener());
        e3.register(fl.get(3).getSetListener());

        parentEvent.register(e1);
        parentEvent.register(e2);
        parentEvent.register(e3, x -> new TestEventArgs(x.getText() + " for e3"));

        parentEvent.invoke(new TestEventArgs("yellow"));

        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");

        parentEvent.clearDependentEvents();
        parentEvent.invoke(new TestEventArgs("purple"));

        fl.get(0).assertEquals("purple");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");
    }

    @Test
    public void clear()
    {
        T parentEvent = getNewEvent();
        T e1 = getNewEvent();
        T e2 = getNewEvent();
        T e3 = getNewEvent();
        FlagList fl = new FlagList(4);

        parentEvent.register(fl.get(0).getSetListener());
        e1.register(fl.get(1).getSetListener());
        e2.register(fl.get(2).getSetListener());
        e3.register(fl.get(3).getSetListener());

        parentEvent.register(e1);
        parentEvent.register(e2);
        parentEvent.register(e3, x -> new TestEventArgs(x.getText() + " for e3"));

        parentEvent.invoke(new TestEventArgs("yellow"));

        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");

        parentEvent.clear();
        parentEvent.invoke(new TestEventArgs("purple"));

        fl.get(0).assertEquals("yellow");
        fl.get(1).assertEquals("yellow");
        fl.get(2).assertEquals("yellow");
        fl.get(3).assertEquals("yellow for e3");
    }
}