package scot.massie.lib.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class PriorityEventTest<T extends PriorityEvent<EventTest.TestEventArgs>> extends EventTest<T>
{
    @Test
    public void invokeListenersWithPriority()
    {
        TestFlag f = new TestFlag("");
        T e = getNewEvent();

        e.register(args -> f.append("third  - " + args.getText() + "\n"), 0);
        e.register(args -> f.append("fifth  - " + args.getText() + "\n"), Double.POSITIVE_INFINITY);
        e.register(args -> f.append("second - " + args.getText() + "\n"), -50);
        e.register(args -> f.append("fourth - " + args.getText() + "\n"), 100);
        e.register(args -> f.append("first  - " + args.getText() + "\n"));

        e.invoke(new TestEventArgs("doot"));

        String expected =   "first  - doot\n"
                          + "second - doot\n"
                          + "third  - doot\n"
                          + "fourth - doot\n"
                          + "fifth  - doot\n";

        assertEquals(expected, f.get());
    }
}