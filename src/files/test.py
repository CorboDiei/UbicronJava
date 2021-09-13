import sys
import datetime
from pathlib import Path

def main(args):
    f = open(str(Path(__file__).parent.absolute()) + "/test.txt", "a")
    f.write("{}, {}\n".format(args[1], datetime.datetime.now()))
    f.close()

if __name__ == "__main__":
    main(sys.argv)