import sys
from pathlib import Path

def main(args):
    f = open(str(Path(__file__).parent.absolute()) + "/test.txt", "a")
    f.write(args[1])
    f.close()

if __name__ == "__main__":
    main(sys.argv)