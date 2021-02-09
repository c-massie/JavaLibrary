package scot.massie.lib.events;

import static org.junit.jupiter.api.Assertions.*;

class SetEventTest extends EventTest<SetEvent<EventTest.TestEventArgs>>
{
    @Override
    public SetEvent<TestEventArgs> getNewEvent()
    { return new SetEvent<>(); }
}