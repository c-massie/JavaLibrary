package scot.massie.lib.events;

import org.junit.jupiter.api.Test;
import scot.massie.lib.events.args.EventArgs;
import scot.massie.lib.utils.wrappers.MutableWrapper;
import scot.massie.lib.utils.wrappers.Wrapper;

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

    public abstract T getNewEvent();

    @Test
    public void registerAndInvoke()
    {
        T e = getNewEvent();
        MutableWrapper<String> flag = new MutableWrapper<>("Flag not set.");

        e.register(args -> flag.set(args.getText()));

        e.invoke(new TestEventArgs("Yellow"));
        assertEquals("Yellow", flag.get());

        e.invoke(new TestEventArgs("Red"));
        assertEquals("Red", flag.get());

        e.invoke(new TestEventArgs("Blue"));
        assertEquals("Blue", flag.get());

        e.invoke(new TestEventArgs("Green"));
        assertEquals("Green", flag.get());
    }
}