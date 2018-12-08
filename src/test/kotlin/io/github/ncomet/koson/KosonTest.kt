package io.github.ncomet.koson

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows

@TestInstance(PER_CLASS)
class KosonTest : WithAssertions {

    @Test
    fun `empty object`() {
        assertThat(obj { }.toString()).isEqualTo("{}")
    }

    @Test
    fun `empty array`() {
        assertThat(arrayØ.toString()).isEqualTo("[]")
    }

    @Test
    fun `object with all possible types of value`() {
        assertThat(obj {
            "string" to "value"
            "int" to 9
            "double" to 7.6
            "float" to 3.2f
            "boolean" to false
            "object" to obj { }
            "emptyArray" to arrayØ
            "array" to array["test"]
            "null" to null
        }.toString()).isEqualTo("{\"string\":\"value\",\"int\":9,\"double\":7.6,\"float\":3.2,\"boolean\":false,\"object\":{},\"emptyArray\":[],\"array\":[\"test\"],\"null\":null}")
    }

    @Test
    internal fun `array with all possible types of value`() {
        assertThat(array["value", 9, 7.6, 3.2f, false, obj { }, arrayØ, array["test"], null].toString())
            .isEqualTo("[\"value\",9,7.6,3.2,false,{},[],[\"test\"],null]")
    }

    @Nested
    inner class ContainingCases : WithAssertions {
        @Test
        fun `object containing array`() {
            assertThat(obj { "array" to arrayØ }.toString()).isEqualTo("{\"array\":[]}")
        }

        @Test
        fun `array containing object`() {
            assertThat(array[obj { }].toString()).isEqualTo("[{}]")
        }

        @Test
        fun `object containing object`() {
            assertThat(obj { "object" to obj { } }.toString()).isEqualTo("{\"object\":{}}")
        }

        @Test
        fun `array containing array`() {
            assertThat(array[arrayØ].toString()).isEqualTo("[[]]")
        }

        @Test
        @Suppress("UNUSED_EXPRESSION")
        fun `expression object not containing a Koson_to() should do nothing`() {
            assertThat(obj { "content" }.toString()).isEqualTo("{}")
        }
    }

    @Nested
    inner class MoreComplexCases : WithAssertions {
        @Test
        fun `testing all types in an object`() {
            val obj = obj {
                "key" to 3.4
                "anotherKey" to array["test", "test2", 1, 2.433, true]
                "nullsAreAllowedToo" to null
            }

            assertThat("$obj")
                .isEqualTo("{\"key\":3.4,\"anotherKey\":[\"test\",\"test2\",1,2.433,true],\"nullsAreAllowedToo\":null}")
        }

        @Test
        fun `testing all types in an object inlined`() {
            val obj = obj {
                "key" to 3.4; "anotherKey" to array["test", "test2", 1, 2.433, true]; "nullsAreAllowedToo" to null
            }

            assertThat("$obj")
                .isEqualTo("{\"key\":3.4,\"anotherKey\":[\"test\",\"test2\",1,2.433,true],\"nullsAreAllowedToo\":null}")
        }

        @Test
        fun `testing all types in an array`() {
            val array = array[
                    "koson",
                    obj {
                        "key" to 3.4
                        "anotherKey" to array["test", "test2", 1, 2.333, true, null]
                    }
            ]

            assertThat("$array")
                .isEqualTo("[\"koson\",{\"key\":3.4,\"anotherKey\":[\"test\",\"test2\",1,2.333,true,null]}]")
        }
    }

    @Nested
    inner class ExceptionCases : WithAssertions {
        @Test
        fun `array must throw exception when illegal element is inserted`() {
            val message = assertThrows<IllegalArgumentException> { array['c'] }.message
            assertThat(message).isEqualTo("value <c> of type [Character] is not one of allowed JSON value types (String, Number, Boolean, null, obj{}, array[...] or arrayØ)")
        }

        @Test
        @Suppress("UNREACHABLE_CODE")
        fun `object must throw exception when illegal element is added`() {
            val message = assertThrows<IllegalArgumentException> {
                obj {
                    "key" to 'c'
                    "flaggedAsUnreachable" to true
                }
            }.message
            assertThat(message).isEqualTo("value <c> of type [Character] is not one of allowed JSON value types (String, Number, Boolean, null, obj{}, array[...] or arrayØ)")
        }

        @Test
        fun `object must throw exception when illegal when duplicate key`() {
            assertThrows<IllegalArgumentException> {
                obj {
                    "key" to "myVal"
                    "key" to 1.65
                }
            }
        }

        @Test
        @Suppress("UNREACHABLE_CODE")
        fun `object containing a Pair_to() function`() {
            val message = assertThrows<IllegalArgumentException> {
                obj {
                    10 to "element"
                    "flaggedAsUnreachable" to 136.36
                }
            }.message
            assertThat(message).isEqualTo("key <10> of (10 to element) must be of type String")
        }

        @Test
        fun `object containing a Pair_to() function with obj {} as key`() {
            val message = assertThrows<IllegalArgumentException> { obj { obj {} to 1.2 } }.message
            assertThat(message).isEqualTo("key <{}> of ({} to 1.2) must be of type String")
        }

        @Test
        fun `object containing a to function with this as a value`() {
            assertThrows<IllegalArgumentException> { obj { "error" to this } }
        }

        @Test
        fun `object containing a Pair_to() function with this as a value`() {
            assertThrows<IllegalArgumentException> { obj { 10 to this } }
        }

        @Test
        @Suppress("UNREACHABLE_CODE")
        fun `object containing a to function with array keyword as value`() {
            val message = assertThrows<IllegalArgumentException> {
                obj {
                    "error" to array
                    "flaggedAs" to "unreachable"
                }
            }.message
            assertThat(message).isEqualTo("<array> keyword cannot be used as value, to describe an empty array, use <arrayØ> instead")
        }

        @Test
        fun `array containing array keyword as value`() {
            val message = assertThrows<IllegalArgumentException> { array[array] }.message
            assertThat(message).isEqualTo("<array> keyword cannot be used as value, to describe an empty array, use <arrayØ> instead")
        }
    }

}