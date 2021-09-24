package scot.massie.lib.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static scot.massie.lib.utils.ControlFlowUtils.*;

public class ControlFlowUtilsTest
{
    @Test
    void nullCoalesce_nothing()
    { assertThat(ControlFlowUtils.<String>nullCoalesce()).isNull(); }

    @Test
    void nullCoalesce_single_null()
    { assertThat(ControlFlowUtils.<String>nullCoalesce(() -> null)).isNull(); }

    @Test
    void nullCoalesce_single_nonNull()
    { assertThat(nullCoalesce(() -> "doot")).isEqualTo("doot"); }

    @Test
    void nullCoalesce_multiple_null()
    { assertThat(ControlFlowUtils.<String>nullCoalesce(() -> null, () -> null, () -> null)).isNull(); }

    @Test
    void nullCoalesce_multiple_firstNonNull()
    { assertThat(nullCoalesce(() -> "doot", () -> null, () -> null)).isEqualTo("doot"); }

    @Test
    void nullCoalesce_multiple_middleNonNull()
    { assertThat(nullCoalesce(() -> null, () -> "doot", () -> null)).isEqualTo("doot"); }

    @Test
    void nullCoalesce_multiple_lastNonNull()
    { assertThat(nullCoalesce(() -> null, () -> null, () -> "doot")).isEqualTo("doot"); }

    @Test
    void nullCoalesce_multiple_allNonNull()
    {  assertThat(nullCoalesce(() -> "doot", () -> "noot", () -> "hoot")).isEqualTo("doot"); }
}
