import { test, expect } from "vitest";
import { paginate } from "../utils/pagination";

test("returns first page correctly", () => {
    const result = paginate([1,2,3,4], 1, 2);
    expect(result).toEqual([1,2]);
});

test("returns second page correctly", () => {
    const result = paginate([1,2,3,4], 2, 2);
    expect(result).toEqual([3,4]);
});

test("handles empty list", () => {
    const result = paginate([], 1, 2);
    expect(result).toEqual([]);
});