package ex.entities

/*
Hotel meal plans define what food is included in a booking,
ranging from room-only (EP) to all-inclusive (AI) packages covering all meals and drinks.
Common types include Bed & Breakfast (BB), Half Board (HB - breakfast and dinner),
and Full Board (FB - all three meals). These options cater to travelers' needs,
from active vacationers to those seeking resort amenities.
*/

enum class Meal {
    RoomOnly,
    AllInclusive,
    BedBreakfast,
    HalfBoard,
    FullBoard
}