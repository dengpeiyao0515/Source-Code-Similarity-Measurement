// 修改程序注释
#include <stdio.h>
#include <string.h>
using namespace std;
int n,sum,a[20];// 修改程序注释
bool b[100]={0},c[100]={0},d[100]={0};
char MAP[25][25];
void print()// 修改程序注释
{   // 修改程序注释
    sum++;
    if(sum<=1)// 修改程序注释
    {

        for(int i=1;i<=n;i++){ // 修改程序注释
            MAP[i][a[i]]='Q';
        }
    }
}// 修改程序注释
void queen(int i)
{// 修改程序注释
    for(int j=1;j<=n;j++)
    {// 修改程序注释
       if((b[j]==0)&&(c[i+j]==0)&&(d[i-j+n]==0))
       {
        a[i]=j;
        b[j]=1;
        c[i+j]=1;
        d[i-j+n]=1;
        if(i==n)
            print();
        else
            queen(i+1);
        b[j]=0;
        c[i+j]=0;
        d[i-j+n]=0;
        if(sum)
            break;
       }
    }
}
int main()
{// 修改程序注释
    while(~scanf("%d",&n)&&n){
        memset(MAP,'.',sizeof(MAP));
        memset(a,0,sizeof(a));
        memset(b,0,sizeof(b));
        memset(c,0,sizeof(c));
        memset(d,0,sizeof(d));
        sum=0;// 修改程序注释
        queen(1);// 修改程序注释
        if(sum){
            for(int i=1;i<=n;i++){// 修改程序注释
                for(int j=1;j<=n;j++)
                    printf("%c ",MAP[i][j]);
                printf("\n");// 修改程序注释
            }
        }// 修改程序注释
        else
            printf("No answer.\n");
        printf("\n");// 修改程序注释
    }
    return 0;
}
