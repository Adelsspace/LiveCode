import { User } from "../types/shared.types";

export const compareUserLists = (a: User[], b: User[]) => {
  return (
    a.length === b.length &&
    a.every((user, index) => user.username === b[index].username)
  );
};
