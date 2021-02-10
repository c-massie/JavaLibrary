package scot.massie.lib.events;

import static org.junit.jupiter.api.Assertions.*;

class OrderedEventTest extends PriorityEventTest<OrderedEvent<EventTest.TestEventArgs>>
{
    @Override
    public OrderedEvent<TestEventArgs> getNewEvent()
    { return new OrderedEvent<>(); }
}